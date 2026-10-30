package com.deadsleep.app.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.core.content.FileProvider;

import com.deadsleep.app.BuildConfig;
import com.deadsleep.app.data.AppDatabase;
import com.deadsleep.app.model.UpdateInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Manages GitHub-driven automatic and mandatory updates, background APK downloading, and APK package installation.
 */
public class UpdateManager {

    public static final String GITHUB_VERSION_JSON_URL =
            "https://raw.githubusercontent.com/tanjeem180hz/Dead-Sleep/main/version.json";

    public static final String GITHUB_DIRECT_APK_URL =
            "https://github.com/tanjeem180hz/Dead-Sleep/raw/main/release/DeadSleep-v1.0.0.apk";

    private static volatile UpdateManager INSTANCE;
    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface UpdateCheckCallback {
        void onCheckFinished(boolean updateAvailable, UpdateInfo updateInfo, String error);
    }

    public interface DownloadListener {
        void onProgress(int percent, long downloadedBytes, long totalBytes);
        void onComplete(File apkFile);
        void onError(String error);
    }

    private UpdateManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static UpdateManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (UpdateManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UpdateManager(context);
                }
            }
        }
        return INSTANCE;
    }

    public boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            NetworkInfo active = cm.getActiveNetworkInfo();
            return active != null && active.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Asynchronously checks GitHub for newer version metadata.
     */
    public void checkForUpdate(UpdateCheckCallback callback) {
        if (!isNetworkAvailable()) {
            mainHandler.post(() -> callback.onCheckFinished(false, null, "No internet connection"));
            return;
        }

        AppDatabase.getExecutor().execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(GITHUB_VERSION_JSON_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(6000);
                conn.setReadTimeout(6000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "DeadSleep-Android/" + BuildConfig.VERSION_NAME);
                conn.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
                conn.setRequestProperty("Pragma", "no-cache");

                int code = conn.getResponseCode();
                if (code == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    reader.close();

                    UpdateInfo info = UpdateInfo.fromJson(sb.toString());
                    if (info != null) {
                        int currentCode = BuildConfig.VERSION_CODE;
                        boolean isNewer = info.getVersionCode() > currentCode;
                        mainHandler.post(() -> callback.onCheckFinished(isNewer, info, null));
                        return;
                    }
                }
                mainHandler.post(() -> callback.onCheckFinished(false, null, "HTTP " + code));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onCheckFinished(false, null, e.getMessage()));
            } finally {
                if (conn != null) {
                    try { conn.disconnect(); } catch (Exception ignored) {}
                }
            }
        });
    }

    /**
     * Downloads the APK file with redirect tracking and emits progress.
     */
    public void downloadApk(String apkUrl, DownloadListener listener) {
        AppDatabase.getExecutor().execute(() -> {
            HttpURLConnection conn = null;
            FileOutputStream fos = null;
            InputStream is = null;
            try {
                String downloadUrl = (apkUrl != null && !apkUrl.isEmpty()) ? apkUrl : GITHUB_DIRECT_APK_URL;
                URL url = new URL(downloadUrl);

                // Handle HTTP redirects (GitHub raw/releases redirects to AWS S3)
                int redirects = 0;
                while (redirects < 5) {
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setInstanceFollowRedirects(true);
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(15000);
                    conn.setRequestProperty("User-Agent", "DeadSleep-Android");

                    int status = conn.getResponseCode();
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                            status == HttpURLConnection.HTTP_MOVED_PERM ||
                            status == 307 || status == 308) {
                        String newUrl = conn.getHeaderField("Location");
                        url = new URL(newUrl);
                        redirects++;
                        conn.disconnect();
                    } else {
                        break;
                    }
                }

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    mainHandler.post(() -> listener.onError("Failed to connect: HTTP " + responseCode));
                    return;
                }

                long fileLength = conn.getContentLengthLong();
                File downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (downloadDir == null) downloadDir = context.getCacheDir();
                File targetFile = new File(downloadDir, "DeadSleep-update.apk");

                if (targetFile.exists()) targetFile.delete();

                is = conn.getInputStream();
                fos = new FileOutputStream(targetFile);

                byte[] buffer = new byte[8192];
                long totalDownloaded = 0;
                int bytesRead;
                int lastPercent = -1;

                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    totalDownloaded += bytesRead;

                    if (fileLength > 0) {
                        int percent = (int) ((totalDownloaded * 100) / fileLength);
                        if (percent != lastPercent) {
                            lastPercent = percent;
                            final long finalDownloaded = totalDownloaded;
                            mainHandler.post(() -> listener.onProgress(percent, finalDownloaded, fileLength));
                        }
                    }
                }

                fos.flush();
                final File finalFile = targetFile;
                mainHandler.post(() -> listener.onComplete(finalFile));

            } catch (Exception e) {
                mainHandler.post(() -> listener.onError("Download error: " + e.getMessage()));
            } finally {
                try { if (is != null) is.close(); } catch (Exception ignored) {}
                try { if (fos != null) fos.close(); } catch (Exception ignored) {}
                if (conn != null) {
                    try { conn.disconnect(); } catch (Exception ignored) {}
                }
            }
        });
    }

    /**
     * Triggers package installer with FileProvider on Android 7+.
     */
    public void installApk(Activity activity, File apkFile) {
        if (activity == null || apkFile == null || !apkFile.exists()) return;

        // Android 8.0+ Check Unknown Sources Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!activity.getPackageManager().canRequestPackageInstalls()) {
                Intent manageIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                manageIntent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivity(manageIntent);
                return;
            }
        }

        try {
            Uri apkUri = FileProvider.getUriForFile(
                    activity,
                    activity.getPackageName() + ".fileprovider",
                    apkFile
            );

            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(installIntent);
        } catch (Exception e) {
            Intent fallback = new Intent(Intent.ACTION_VIEW);
            fallback.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                activity.startActivity(fallback);
            } catch (Exception ignored) {}
        }
    }
}
