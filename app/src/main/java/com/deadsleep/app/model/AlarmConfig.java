package com.deadsleep.app.model;

/**
 * POJO holding configured parameters for an alarm execution.
 */
public class AlarmConfig {
    private final long scheduleId;
    private final String label;
    private final String ringtoneUri;
    private final int volume;
    private final boolean vibrate;
    private final boolean gradualVolume;
    private final int snoozeDurationMinutes;
    private final int maxSnoozeCount;
    private final String challengeType;
    private final String challengeDifficulty;

    public AlarmConfig(long scheduleId, String label, String ringtoneUri, int volume,
                       boolean vibrate, boolean gradualVolume, int snoozeDurationMinutes,
                       int maxSnoozeCount, String challengeType, String challengeDifficulty) {
        this.scheduleId = scheduleId;
        this.label = label;
        this.ringtoneUri = ringtoneUri;
        this.volume = volume;
        this.vibrate = vibrate;
        this.gradualVolume = gradualVolume;
        this.snoozeDurationMinutes = snoozeDurationMinutes;
        this.maxSnoozeCount = maxSnoozeCount;
        this.challengeType = challengeType;
        this.challengeDifficulty = challengeDifficulty;
    }

    public long getScheduleId() { return scheduleId; }
    public String getLabel() { return label; }
    public String getRingtoneUri() { return ringtoneUri; }
    public int getVolume() { return volume; }
    public boolean isVibrate() { return vibrate; }
    public boolean isGradualVolume() { return gradualVolume; }
    public int getSnoozeDurationMinutes() { return snoozeDurationMinutes; }
    public int getMaxSnoozeCount() { return maxSnoozeCount; }
    public String getChallengeType() { return challengeType; }
    public String getChallengeDifficulty() { return challengeDifficulty; }
}
