package com.deadsleep.app.model;

/**
 * POJO holding user profile preferences collected during onboarding or settings.
 */
public class UserProfile {
    private String name;
    private int usualBedtimeHour;
    private int usualBedtimeMinute;
    private int usualWakeHour;
    private int usualWakeMinute;
    private int targetSleepHours;
    private String preferredDifficulty;
    private String preferredChallenge;

    public UserProfile() {
        this.name = "";
        this.usualBedtimeHour = 23;
        this.usualBedtimeMinute = 0;
        this.usualWakeHour = 7;
        this.usualWakeMinute = 0;
        this.targetSleepHours = 8;
        this.preferredDifficulty = "MEDIUM";
        this.preferredChallenge = "RANDOM";
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getUsualBedtimeHour() { return usualBedtimeHour; }
    public void setUsualBedtimeHour(int usualBedtimeHour) { this.usualBedtimeHour = usualBedtimeHour; }

    public int getUsualBedtimeMinute() { return usualBedtimeMinute; }
    public void setUsualBedtimeMinute(int usualBedtimeMinute) { this.usualBedtimeMinute = usualBedtimeMinute; }

    public int getUsualWakeHour() { return usualWakeHour; }
    public void setUsualWakeHour(int usualWakeHour) { this.usualWakeHour = usualWakeHour; }

    public int getUsualWakeMinute() { return usualWakeMinute; }
    public void setUsualWakeMinute(int usualWakeMinute) { this.usualWakeMinute = usualWakeMinute; }

    public int getTargetSleepHours() { return targetSleepHours; }
    public void setTargetSleepHours(int targetSleepHours) { this.targetSleepHours = targetSleepHours; }

    public String getPreferredDifficulty() { return preferredDifficulty; }
    public void setPreferredDifficulty(String preferredDifficulty) { this.preferredDifficulty = preferredDifficulty; }

    public String getPreferredChallenge() { return preferredChallenge; }
    public void setPreferredChallenge(String preferredChallenge) { this.preferredChallenge = preferredChallenge; }
}
