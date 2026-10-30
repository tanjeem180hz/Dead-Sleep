package com.deadsleep.app.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.deadsleep.app.util.Constants;
import com.deadsleep.app.util.TimeUtils;

/**
 * Represents a user-defined sleep and wake-up schedule.
 */
@Entity(tableName = "sleep_schedules")
public class SleepSchedule {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private int sleepHour;
    private int sleepMinute;
    private int wakeHour;
    private int wakeMinute;
    private int daysOfWeek; // Bitmask: Mon=1, Tue=2, Wed=4, Thu=8, Fri=16, Sat=32, Sun=64
    private boolean isEnabled;

    // Alarm configuration for this schedule
    private String ringtoneUri;
    private String ringtoneTitle;
    private int volume; // 1-100
    private boolean vibrate;
    private boolean gradualVolume;
    private int snoozeDurationMinutes;
    private int maxSnoozeCount;

    // Challenge configuration
    private String challengeType; // RANDOM, MATH, MEMORY, PATTERN, REACTION, SHAKE
    private String challengeDifficulty; // EASY, MEDIUM, HARD
    private boolean autoEscalateChallenge;

    private long createdAt;

    public SleepSchedule() {
        this.name = "Bedtime Routine";
        this.sleepHour = Constants.DEFAULT_SLEEP_HOUR;
        this.sleepMinute = Constants.DEFAULT_SLEEP_MINUTE;
        this.wakeHour = Constants.DEFAULT_WAKE_HOUR;
        this.wakeMinute = Constants.DEFAULT_WAKE_MINUTE;
        this.daysOfWeek = TimeUtils.ALL_DAYS;
        this.isEnabled = true;
        this.ringtoneUri = "bundled:tone_gentle_chime";
        this.ringtoneTitle = "Gentle Chime";
        this.volume = 85;
        this.vibrate = true;
        this.gradualVolume = true;
        this.snoozeDurationMinutes = Constants.DEFAULT_SNOOZE_DURATION_MINUTES;
        this.maxSnoozeCount = Constants.DEFAULT_MAX_SNOOZE_COUNT;
        this.challengeType = Constants.CHALLENGE_RANDOM;
        this.challengeDifficulty = Constants.DIFFICULTY_MEDIUM;
        this.autoEscalateChallenge = true;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getSleepHour() { return sleepHour; }
    public void setSleepHour(int sleepHour) { this.sleepHour = sleepHour; }

    public int getSleepMinute() { return sleepMinute; }
    public void setSleepMinute(int sleepMinute) { this.sleepMinute = sleepMinute; }

    public int getWakeHour() { return wakeHour; }
    public void setWakeHour(int wakeHour) { this.wakeHour = wakeHour; }

    public int getWakeMinute() { return wakeMinute; }
    public void setWakeMinute(int wakeMinute) { this.wakeMinute = wakeMinute; }

    public int getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(int daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }

    public String getRingtoneUri() { return ringtoneUri; }
    public void setRingtoneUri(String ringtoneUri) { this.ringtoneUri = ringtoneUri; }

    public String getRingtoneTitle() { return ringtoneTitle; }
    public void setRingtoneTitle(String ringtoneTitle) { this.ringtoneTitle = ringtoneTitle; }

    public int getVolume() { return volume; }
    public void setVolume(int volume) { this.volume = volume; }

    public boolean isVibrate() { return vibrate; }
    public void setVibrate(boolean vibrate) { this.vibrate = vibrate; }

    public boolean isGradualVolume() { return gradualVolume; }
    public void setGradualVolume(boolean gradualVolume) { this.gradualVolume = gradualVolume; }

    public int getSnoozeDurationMinutes() { return snoozeDurationMinutes; }
    public void setSnoozeDurationMinutes(int snoozeDurationMinutes) { this.snoozeDurationMinutes = snoozeDurationMinutes; }

    public int getMaxSnoozeCount() { return maxSnoozeCount; }
    public void setMaxSnoozeCount(int maxSnoozeCount) { this.maxSnoozeCount = maxSnoozeCount; }

    public String getChallengeType() { return challengeType; }
    public void setChallengeType(String challengeType) { this.challengeType = challengeType; }

    public String getChallengeDifficulty() { return challengeDifficulty; }
    public void setChallengeDifficulty(String challengeDifficulty) { this.challengeDifficulty = challengeDifficulty; }

    public boolean isAutoEscalateChallenge() { return autoEscalateChallenge; }
    public void setAutoEscalateChallenge(boolean autoEscalateChallenge) { this.autoEscalateChallenge = autoEscalateChallenge; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Helpers
    public String getFormattedSleepTime() {
        return TimeUtils.formatTime(sleepHour, sleepMinute);
    }

    public String getFormattedSleepTime(android.content.Context context) {
        return TimeUtils.formatTime(context, sleepHour, sleepMinute);
    }

    public String getFormattedWakeTime() {
        return TimeUtils.formatTime(wakeHour, wakeMinute);
    }

    public String getFormattedWakeTime(android.content.Context context) {
        return TimeUtils.formatTime(context, wakeHour, wakeMinute);
    }

    public int getDurationMinutes() {
        return TimeUtils.calculateDurationMinutes(sleepHour, sleepMinute, wakeHour, wakeMinute);
    }

    public String getFormattedDuration() {
        return TimeUtils.formatDuration(getDurationMinutes());
    }

    public String getFormattedDays() {
        return TimeUtils.formatDays(daysOfWeek);
    }

    public boolean isDayActive(int calendarDay) {
        return TimeUtils.isDayActive(daysOfWeek, calendarDay);
    }
}
