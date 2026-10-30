package com.deadsleep.app.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.deadsleep.app.util.Constants;

/**
 * Encapsulated SharedPreferences wrapper for app-wide settings and runtime states.
 */
public class PreferencesManager {

    private static final String PREF_NAME = "dead_sleep_preferences";

    // Keys
    private static final String KEY_ONBOARDING_COMPLETED = "key_onboarding_completed";
    private static final String KEY_SLEEP_MODE_ACTIVE = "key_sleep_mode_active";
    private static final String KEY_ACTIVE_SESSION_ID = "key_active_session_id";
    private static final String KEY_CURRENT_SCHEDULE_ID = "key_current_schedule_id";
    private static final String KEY_NUDGE_LEAD_MINUTES = "key_nudge_lead_minutes";
    private static final String KEY_SECOND_NUDGE_ENABLED = "key_second_nudge_enabled";
    private static final String KEY_BLOCK_NOTIFICATIONS = "key_block_notifications";
    private static final String KEY_USER_NAME = "key_user_name";
    private static final String KEY_DEFAULT_DIFFICULTY = "key_default_difficulty";
    private static final String KEY_DEFAULT_CHALLENGE = "key_default_challenge";
    private static final String KEY_THEME_MODE = "key_theme_mode"; // -1 system, 1 light, 2 dark
    private static final String KEY_DYNAMIC_COLOR = "key_dynamic_color";
    private static final String KEY_EMERGENCY_STOP_TIMEOUT_MIN = "key_emergency_stop_timeout_min";
    private static final String KEY_CURRENT_STREAK = "key_current_streak";
    private static final String KEY_BEST_STREAK = "key_best_streak";
    private static final String KEY_LAST_STREAK_DATE = "key_last_streak_date";
    private static final String KEY_BEDTIME_MODE_AUTO_ENABLED = "key_bedtime_mode_auto_enabled";
    private static final String KEY_BEDTIME_GRAYSCALE_ENABLED = "key_bedtime_grayscale_enabled";
    private static final String KEY_BEDTIME_DND_ENABLED = "key_bedtime_dnd_enabled";

    private static volatile PreferencesManager INSTANCE;
    private final SharedPreferences prefs;

    private PreferencesManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static PreferencesManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (PreferencesManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PreferencesManager(context);
                }
            }
        }
        return INSTANCE;
    }

    // Onboarding
    public boolean isOnboardingCompleted() {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }

    public void setOnboardingCompleted(boolean completed) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply();
    }

    // Sleep Mode State
    public boolean isSleepModeActive() {
        return prefs.getBoolean(KEY_SLEEP_MODE_ACTIVE, false);
    }

    public void setSleepModeActive(boolean active) {
        prefs.edit().putBoolean(KEY_SLEEP_MODE_ACTIVE, active).apply();
    }

    public long getActiveSessionId() {
        return prefs.getLong(KEY_ACTIVE_SESSION_ID, -1);
    }

    public void setActiveSessionId(long sessionId) {
        prefs.edit().putLong(KEY_ACTIVE_SESSION_ID, sessionId).apply();
    }

    public long getCurrentScheduleId() {
        return prefs.getLong(KEY_CURRENT_SCHEDULE_ID, -1);
    }

    public void setCurrentScheduleId(long scheduleId) {
        prefs.edit().putLong(KEY_CURRENT_SCHEDULE_ID, scheduleId).apply();
    }

    // Reminders
    public int getNudgeLeadMinutes() {
        return prefs.getInt(KEY_NUDGE_LEAD_MINUTES, Constants.DEFAULT_NUDGE_LEAD_MINUTES);
    }

    public void setNudgeLeadMinutes(int minutes) {
        prefs.edit().putInt(KEY_NUDGE_LEAD_MINUTES, minutes).apply();
    }

    public boolean isSecondNudgeEnabled() {
        return prefs.getBoolean(KEY_SECOND_NUDGE_ENABLED, true);
    }

    public void setSecondNudgeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SECOND_NUDGE_ENABLED, enabled).apply();
    }

    // Restrictions
    public boolean isBlockNotificationsEnabled() {
        return prefs.getBoolean(KEY_BLOCK_NOTIFICATIONS, false);
    }

    public void setBlockNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BLOCK_NOTIFICATIONS, enabled).apply();
    }

    // User Profile
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "Friend");
    }

    public void setUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    public String getDefaultDifficulty() {
        return prefs.getString(KEY_DEFAULT_DIFFICULTY, Constants.DIFFICULTY_MEDIUM);
    }

    public void setDefaultDifficulty(String difficulty) {
        prefs.edit().putString(KEY_DEFAULT_DIFFICULTY, difficulty).apply();
    }

    public String getDefaultChallenge() {
        return prefs.getString(KEY_DEFAULT_CHALLENGE, Constants.CHALLENGE_RANDOM);
    }

    public void setDefaultChallenge(String challenge) {
        prefs.edit().putString(KEY_DEFAULT_CHALLENGE, challenge).apply();
    }

    // Theme & Appearance
    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    public boolean isDynamicColorEnabled() {
        return prefs.getBoolean(KEY_DYNAMIC_COLOR, true);
    }

    public void setDynamicColorEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply();
    }

    // Emergency Timeout
    public int getEmergencyStopTimeoutMinutes() {
        return prefs.getInt(KEY_EMERGENCY_STOP_TIMEOUT_MIN, 20);
    }

    public void setEmergencyStopTimeoutMinutes(int minutes) {
        prefs.edit().putInt(KEY_EMERGENCY_STOP_TIMEOUT_MIN, minutes).apply();
    }

    // Streaks
    public int getCurrentStreak() {
        return prefs.getInt(KEY_CURRENT_STREAK, 0);
    }

    public void setCurrentStreak(int streak) {
        prefs.edit().putInt(KEY_CURRENT_STREAK, streak).apply();
    }

    public int getBestStreak() {
        return prefs.getInt(KEY_BEST_STREAK, 0);
    }

    public void setBestStreak(int streak) {
        prefs.edit().putInt(KEY_BEST_STREAK, streak).apply();
    }

    public String getLastStreakDate() {
        return prefs.getString(KEY_LAST_STREAK_DATE, "");
    }

    public void setLastStreakDate(String date) {
        prefs.edit().putString(KEY_LAST_STREAK_DATE, date).apply();
    }

    // Bedtime Mode Settings
    public boolean isBedtimeModeAutoEnabled() {
        return prefs.getBoolean(KEY_BEDTIME_MODE_AUTO_ENABLED, true);
    }

    public void setBedtimeModeAutoEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BEDTIME_MODE_AUTO_ENABLED, enabled).apply();
    }

    public boolean isBedtimeGrayscaleEnabled() {
        return prefs.getBoolean(KEY_BEDTIME_GRAYSCALE_ENABLED, true);
    }

    public void setBedtimeGrayscaleEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BEDTIME_GRAYSCALE_ENABLED, enabled).apply();
    }

    public boolean isBedtimeDndEnabled() {
        return prefs.getBoolean(KEY_BEDTIME_DND_ENABLED, false);
    }

    public void setBedtimeDndEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BEDTIME_DND_ENABLED, enabled).apply();
    }

    public void resetAll() {
        prefs.edit().clear().apply();
    }
}
