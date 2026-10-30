package com.deadsleep.app.ui;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.deadsleep.app.BuildConfig;
import com.deadsleep.app.databinding.ActivityForceUpdateBinding;
import com.deadsleep.app.manager.UpdateManager;
import com.deadsleep.app.model.UpdateInfo;

import java.io.File;
import java.util.Locale;

/**
 * Mandatory full-screen update screen that blocks app usage until the latest GitHub release is installed.
 */
public class ForceUpdateActivity extends AppCompatActivity {

    public static final String EXTRA_UPDATE_INFO = "update_info";

    private ActivityForceUpdateBinding binding;
    private UpdateInfo updateInfo;
    private File downloadedApkFile;
    private boolean isDownloading = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForceUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        updateInfo = (UpdateInfo) getIntent().getSerializableExtra(EXTRA_UPDATE_INFO);

        setupUI();
        setupListeners();

        // Enforce mandatory update: disable back navigation
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(
                        ForceUpdateActivity.this,
                        "⚠️ You must install the update to continue using Dead Sleep.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void setupUI() {
        binding.tvCurrentVersion.setText("Current: v" + BuildConfig.VERSION_NAME + " (Build " + BuildConfig.VERSION_CODE + ")");

        if (updateInfo != null) {
            binding.tvNewVersion.setText("Latest: v" + updateInfo.getVersionName() + " (Build " + updateInfo.getVersionCode() + ") ⚡");
            if (updateInfo.getTitle() != null && !updateInfo.getTitle().isEmpty()) {
                binding.tvUpdateTitle.setText(updateInfo.getTitle());
            }
            if (updateInfo.getReleaseNotes() != null && !updateInfo.getReleaseNotes().isEmpty()) {
                binding.tvReleaseNotes.setText(updateInfo.getReleaseNotes());
            }
        } else {
            // Fetch metadata in background
            UpdateManager.getInstance(this).checkForUpdate((updateAvailable, info, error) -> {
                if (isFinishing() || isDestroyed()) return;
                if (info != null) {
                    updateInfo = info;
                    binding.tvNewVersion.setText("Latest: v" + info.getVersionName() + " (Build " + info.getVersionCode() + ") ⚡");
                    if (info.getReleaseNotes() != null && !info.getReleaseNotes().isEmpty()) {
                        binding.tvReleaseNotes.setText(info.getReleaseNotes());
                    }
                }
            });
        }
    }

    private void setupListeners() {
        binding.btnDownloadUpdate.setOnClickListener(v -> {
            if (downloadedApkFile != null && downloadedApkFile.exists()) {
                UpdateManager.getInstance(this).installApk(this, downloadedApkFile);
                return;
            }
            startDownload();
        });

        binding.btnExitApp.setOnClickListener(v -> {
            finishAffinity();
        });
    }

    private void startDownload() {
        if (isDownloading) return;
        isDownloading = true;

        binding.layoutProgress.setVisibility(View.VISIBLE);
        binding.btnDownloadUpdate.setEnabled(false);
        binding.btnDownloadUpdate.setText("Downloading Update...");

        String apkUrl = (updateInfo != null) ? updateInfo.getApkUrl() : UpdateManager.GITHUB_DIRECT_APK_URL;

        UpdateManager.getInstance(this).downloadApk(apkUrl, new UpdateManager.DownloadListener() {
            @Override
            public void onProgress(int percent, long downloadedBytes, long totalBytes) {
                if (isFinishing() || isDestroyed()) return;
                binding.pbDownload.setProgress(percent);
                binding.tvDownloadPercent.setText(percent + "%");

                if (totalBytes > 0) {
                    double dlMb = downloadedBytes / (1024.0 * 1024.0);
                    double totalMb = totalBytes / (1024.0 * 1024.0);
                    binding.tvDownloadStatus.setText(String.format(Locale.US, "Downloading: %.1f MB / %.1f MB", dlMb, totalMb));
                } else {
                    binding.tvDownloadStatus.setText("Downloading latest release...");
                }
            }

            @Override
            public void onComplete(File apkFile) {
                if (isFinishing() || isDestroyed()) return;
                isDownloading = false;
                downloadedApkFile = apkFile;

                binding.layoutProgress.setVisibility(View.GONE);
                binding.btnDownloadUpdate.setEnabled(true);
                binding.btnDownloadUpdate.setText("📦 Tap to Install Update");

                Toast.makeText(ForceUpdateActivity.this, "Download complete! Launching installer...", Toast.LENGTH_SHORT).show();
                UpdateManager.getInstance(ForceUpdateActivity.this).installApk(ForceUpdateActivity.this, apkFile);
            }

            @Override
            public void onError(String error) {
                if (isFinishing() || isDestroyed()) return;
                isDownloading = false;
                binding.btnDownloadUpdate.setEnabled(true);
                binding.btnDownloadUpdate.setText("Retry Download 🚀");
                binding.tvDownloadStatus.setText("Error: " + error);
                Toast.makeText(ForceUpdateActivity.this, "Download failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if user returned from Settings after granting unknown sources
        if (downloadedApkFile != null && downloadedApkFile.exists()) {
            binding.btnDownloadUpdate.setText("📦 Tap to Install Update");
        }
    }
}
