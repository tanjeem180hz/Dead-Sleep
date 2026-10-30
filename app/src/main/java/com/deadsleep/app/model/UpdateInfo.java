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
        try {
            JSONObject json = new JSONObject(jsonStr);
            UpdateInfo info = new UpdateInfo();
            info.versionCode = json.optInt("versionCode", 1);
            info.versionName = json.optString("versionName", "1.0.0");
            info.apkUrl = json.optString("apkUrl", "https://github.com/tanjeem180hz/Dead-Sleep/raw/main/release/DeadSleep-v1.0.0.apk");
            info.forceUpdate = json.optBoolean("forceUpdate", true);
            info.title = json.optString("title", "Update Available");
            info.releaseNotes = json.optString("releaseNotes", "New improvements and bug fixes.");
            info.minSupportedVersionCode = json.optInt("minSupportedVersionCode", 1);
            return info;
        } catch (Exception e) {
            return null;
        }
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
