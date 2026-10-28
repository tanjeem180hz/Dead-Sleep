package com.deadsleep.app.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Records an active or completed sleep session for sleep statistics and insights.
 */
@Entity(tableName = "sleep_sessions")
public class SleepSession {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long scheduleId;
    private String scheduleName;
    private long startTimeMillis;
    private long endTimeMillis;
    private long targetWakeTimeMillis;
    private long actualWakeTimeMillis;
    private boolean isCompleted;
    private int snoozesUsed;
    private int blockedAttemptsCount;
    private int challengeSolveTimeSec;
    private String challengeType;
    private String status; // ACTIVE, COMPLETED, EMERGENCY_UNLOCKED

    public SleepSession() {
        this.startTimeMillis = System.currentTimeMillis();
        this.isCompleted = false;
        this.snoozesUsed = 0;
        this.blockedAttemptsCount = 0;
        this.challengeSolveTimeSec = 0;
        this.status = "ACTIVE";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getScheduleId() { return scheduleId; }
    public void setScheduleId(long scheduleId) { this.scheduleId = scheduleId; }

    public String getScheduleName() { return scheduleName; }
    public void setScheduleName(String scheduleName) { this.scheduleName = scheduleName; }

    public long getStartTimeMillis() { return startTimeMillis; }
    public void setStartTimeMillis(long startTimeMillis) { this.startTimeMillis = startTimeMillis; }

    public long getEndTimeMillis() { return endTimeMillis; }
    public void setEndTimeMillis(long endTimeMillis) { this.endTimeMillis = endTimeMillis; }

    public long getTargetWakeTimeMillis() { return targetWakeTimeMillis; }
    public void setTargetWakeTimeMillis(long targetWakeTimeMillis) { this.targetWakeTimeMillis = targetWakeTimeMillis; }

    public long getActualWakeTimeMillis() { return actualWakeTimeMillis; }
    public void setActualWakeTimeMillis(long actualWakeTimeMillis) { this.actualWakeTimeMillis = actualWakeTimeMillis; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public int getSnoozesUsed() { return snoozesUsed; }
    public void setSnoozesUsed(int snoozesUsed) { this.snoozesUsed = snoozesUsed; }

    public int getBlockedAttemptsCount() { return blockedAttemptsCount; }
    public void setBlockedAttemptsCount(int blockedAttemptsCount) { this.blockedAttemptsCount = blockedAttemptsCount; }

    public int getChallengeSolveTimeSec() { return challengeSolveTimeSec; }
    public void setChallengeSolveTimeSec(int challengeSolveTimeSec) { this.challengeSolveTimeSec = challengeSolveTimeSec; }

    public String getChallengeType() { return challengeType; }
    public void setChallengeType(String challengeType) { this.challengeType = challengeType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getActualDurationMinutes() {
        if (actualWakeTimeMillis > startTimeMillis) {
            return (int) ((actualWakeTimeMillis - startTimeMillis) / (60 * 1000));
        } else if (endTimeMillis > startTimeMillis) {
            return (int) ((endTimeMillis - startTimeMillis) / (60 * 1000));
        }
        return 0;
    }
}
