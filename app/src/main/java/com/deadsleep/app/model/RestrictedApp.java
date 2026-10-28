package com.deadsleep.app.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents an installed application targeted for restriction during sleep mode.
 */
@Entity(tableName = "restricted_apps")
public class RestrictedApp {

    @PrimaryKey
    @NonNull
    private String packageName;

    private String appName;
    private String category; // SOCIAL, GAMES, VIDEO, BROWSERS, OTHER
    private boolean isRestricted;
    private boolean isPreChecked;
    private int blockCount;
    private long totalUsageTimeMillis;

    public RestrictedApp(@NonNull String packageName, String appName, String category, boolean isRestricted) {
        this.packageName = packageName;
        this.appName = appName;
        this.category = category;
        this.isRestricted = isRestricted;
        this.isPreChecked = isRestricted;
        this.blockCount = 0;
        this.totalUsageTimeMillis = 0;
    }

    @NonNull
    public String getPackageName() { return packageName; }
    public void setPackageName(@NonNull String packageName) { this.packageName = packageName; }

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isRestricted() { return isRestricted; }
    public void setRestricted(boolean restricted) { isRestricted = restricted; }

    public boolean isPreChecked() { return isPreChecked; }
    public void setPreChecked(boolean preChecked) { isPreChecked = preChecked; }

    public int getBlockCount() { return blockCount; }
    public void setBlockCount(int blockCount) { this.blockCount = blockCount; }

    public long getTotalUsageTimeMillis() { return totalUsageTimeMillis; }
    public void setTotalUsageTimeMillis(long totalUsageTimeMillis) { this.totalUsageTimeMillis = totalUsageTimeMillis; }
}
