package com.deadsleep.app.util;

/**
 * Global constants for the Dead Sleep application.
 */
public final class Constants {

    private Constants() {}

    // Package and Application Info
    public static final String PACKAGE_NAME = "com.deadsleep.app";
    public static final String APP_NAME = "Dead Sleep";
    public static final String TAGLINE = "Sleep on time. Wake up on time.";

    // Notification Channel IDs
    public static final String CHANNEL_ID_NUDGE = "channel_deadsleep_nudge";
    public static final String CHANNEL_NAME_NUDGE = "Bedtime Nudges";
    public static final String CHANNEL_ID_SLEEP_MODE = "channel_deadsleep_sleep_mode";
    public static final String CHANNEL_NAME_SLEEP_MODE = "Sleep Mode Active";
    public static final String CHANNEL_ID_ALARM = "channel_deadsleep_alarm";
    public static final String CHANNEL_NAME_ALARM = "Wake-up Alarm";
    public static final String CHANNEL_ID_STATS = "channel_deadsleep_stats";
    public static final String CHANNEL_NAME_STATS = "Insights & Streaks";

    // Notification IDs
    public static final int NOTIF_ID_NUDGE = 1001;
    public static final int NOTIF_ID_NUDGE_FINAL = 1002;
    public static final int NOTIF_ID_SLEEP_MODE = 2001;
    public static final int NOTIF_ID_ALARM = 3001;
    public static final int NOTIF_ID_PERMISSION_WARN = 4001;

    // Intent Actions
    public static final String ACTION_START_SLEEP_NOW = "com.deadsleep.app.ACTION_START_SLEEP_NOW";
    public static final String ACTION_SNOOZE_NUDGE = "com.deadsleep.app.ACTION_SNOOZE_NUDGE";
    public static final String ACTION_START_SLEEP_MODE = "com.deadsleep.app.ACTION_START_SLEEP_MODE";
    public static final String ACTION_END_SLEEP_MODE = "com.deadsleep.app.ACTION_END_SLEEP_MODE";
    public static final String ACTION_TRIGGER_ALARM = "com.deadsleep.app.ACTION_TRIGGER_ALARM";
    public static final String ACTION_SNOOZE_ALARM = "com.deadsleep.app.ACTION_SNOOZE_ALARM";
    public static final String ACTION_DISMISS_ALARM = "com.deadsleep.app.ACTION_DISMISS_ALARM";
    public static final String ACTION_EMERGENCY_UNLOCK = "com.deadsleep.app.ACTION_EMERGENCY_UNLOCK";
    public static final String ACTION_SCHEDULE_CHANGED = "com.deadsleep.app.ACTION_SCHEDULE_CHANGED";

    // Intent Extras
    public static final String EXTRA_SCHEDULE_ID = "extra_schedule_id";
    public static final String EXTRA_SCHEDULE_NAME = "extra_schedule_name";
    public static final String EXTRA_SESSION_ID = "extra_session_id";
    public static final String EXTRA_CHALLENGE_TYPE = "extra_challenge_type";
    public static final String EXTRA_CHALLENGE_DIFFICULTY = "extra_challenge_difficulty";
    public static final String EXTRA_IS_PRACTICE = "extra_is_practice";
    public static final String EXTRA_IS_TEST_ALARM = "extra_is_test_alarm";
    public static final String EXTRA_BLOCKED_PACKAGE = "extra_blocked_package";
    public static final String EXTRA_BLOCKED_APP_NAME = "extra_blocked_app_name";

    // Challenge Types
    public static final String CHALLENGE_RANDOM = "RANDOM";
    public static final String CHALLENGE_MATH = "MATH";
    public static final String CHALLENGE_MEMORY = "MEMORY";
    public static final String CHALLENGE_PATTERN = "PATTERN";
    public static final String CHALLENGE_REACTION = "REACTION";
    public static final String CHALLENGE_SHAKE = "SHAKE";

    // Difficulty Levels
    public static final String DIFFICULTY_EASY = "EASY";
    public static final String DIFFICULTY_MEDIUM = "MEDIUM";
    public static final String DIFFICULTY_HARD = "HARD";

    // App Categories
    public static final String CATEGORY_SOCIAL = "SOCIAL";
    public static final String CATEGORY_GAMES = "GAMES";
    public static final String CATEGORY_VIDEO = "VIDEO";
    public static final String CATEGORY_BROWSERS = "BROWSERS";
    public static final String CATEGORY_OTHER = "OTHER";

    // Whitelist Types
    public static final String WHITELIST_TYPE_APP = "APP";
    public static final String WHITELIST_TYPE_CONTACT = "CONTACT";

    // Default Values
    public static final int DEFAULT_SLEEP_HOUR = 23;
    public static final int DEFAULT_SLEEP_MINUTE = 0;
    public static final int DEFAULT_WAKE_HOUR = 7;
    public static final int DEFAULT_WAKE_MINUTE = 0;
    public static final int DEFAULT_NUDGE_LEAD_MINUTES = 30;
    public static final int DEFAULT_SNOOZE_DURATION_MINUTES = 5;
    public static final int DEFAULT_MAX_SNOOZE_COUNT = 3;
    public static final int DEFAULT_TARGET_SLEEP_HOURS = 8;
    public static final int EMERGENCY_HOLD_DURATION_SECONDS = 10;
}
