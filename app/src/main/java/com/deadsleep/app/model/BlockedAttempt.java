package com.deadsleep.app.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Logs an individual attempt to open a restricted application during sleep hours.
 */
@Entity(tableName = "blocked_attempts")
public class BlockedAttempt {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String packageName;
    private String appName;
    private long timestampMillis;
    private String dateStr; // yyyy-MM-dd

    public BlockedAttempt(String packageName, String appName, long timestampMillis, String dateStr) {
        this.packageName = packageName;
        this.appName = appName;
        this.timestampMillis = timestampMillis;
        this.dateStr = dateStr;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    public long getTimestampMillis() { return timestampMillis; }
    public void setTimestampMillis(long timestampMillis) { this.timestampMillis = timestampMillis; }

    public String getDateStr() { return dateStr; }
    public void setDateStr(String dateStr) { this.dateStr = dateStr; }
}
