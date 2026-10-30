package com.deadsleep.app.model;

import org.json.JSONObject;
import java.io.Serializable;

/**
 * Encapsulates remote GitHub version metadata for automatic and mandatory updates.
 */
public class UpdateInfo implements Serializable {

    private int versionCode;
    private String versionName;
    private String apkUrl;
    private boolean forceUpdate;
    private String title;
    private String releaseNotes;
    private int minSupportedVersionCode;

    public UpdateInfo() {}

    public static UpdateInfo fromJson(String jsonStr) {
        if (jsonStr == null || !jsonStr.contains("{") || !jsonStr.contains("}")) {
            return null;
        }
        try {
            int code = extractInt(jsonStr, "versionCode", -1);
            if (code <= 0) return null;

            UpdateInfo info = new UpdateInfo();
            info.versionCode = code;
            info.versionName = extractString(jsonStr, "versionName", "1.0.0");
            info.apkUrl = extractString(jsonStr, "apkUrl", "https://github.com/tanjeem180hz/Dead-Sleep/raw/main/release/DeadSleep-v1.0.0.apk");
            info.forceUpdate = extractBoolean(jsonStr, "forceUpdate", true);
            info.title = extractString(jsonStr, "title", "Update Available");
            info.releaseNotes = extractString(jsonStr, "releaseNotes", "New improvements and bug fixes.");
            info.minSupportedVersionCode = extractInt(jsonStr, "minSupportedVersionCode", 1);
            return info;
        } catch (Exception e) {
            return null;
        }
    }

    private static String extractString(String json, String key, String defaultVal) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1).replace("\\n", "\n").replace("\\\"", "\"");
        }
        return defaultVal;
    }

    private static int extractInt(String json, String key, int defaultVal) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)");
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return defaultVal;
    }

    private static boolean extractBoolean(String json, String key, boolean defaultVal) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*(true|false)");
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return Boolean.parseBoolean(m.group(1));
        }
        return defaultVal;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getApkUrl() {
        return apkUrl;
    }

    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public int getMinSupportedVersionCode() {
        return minSupportedVersionCode;
    }

    public void setMinSupportedVersionCode(int minSupportedVersionCode) {
        this.minSupportedVersionCode = minSupportedVersionCode;
    }
}
