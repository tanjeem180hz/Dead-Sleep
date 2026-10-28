package com.deadsleep.app.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.deadsleep.app.model.BlockedAttempt;
import com.deadsleep.app.model.ChallengeResult;
import com.deadsleep.app.model.RestrictedApp;
import com.deadsleep.app.model.SleepSchedule;
import com.deadsleep.app.model.SleepSession;
import com.deadsleep.app.model.WhitelistEntry;
import com.deadsleep.app.util.Constants;
import com.deadsleep.app.util.TimeUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main Room Database for the Dead Sleep application.
 */
@Database(
        entities = {
                SleepSchedule.class,
                RestrictedApp.class,
                WhitelistEntry.class,
                SleepSession.class,
                ChallengeResult.class,
                BlockedAttempt.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final ExecutorService DB_EXECUTOR = Executors.newFixedThreadPool(4);

    public abstract SleepScheduleDao sleepScheduleDao();
    public abstract RestrictedAppDao restrictedAppDao();
    public abstract WhitelistDao whitelistDao();
    public abstract SleepSessionDao sleepSessionDao();
    public abstract ChallengeResultDao challengeResultDao();
    public abstract BlockedAttemptDao blockedAttemptDao();

    public static ExecutorService getExecutor() {
        return DB_EXECUTOR;
    }

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "dead_sleep_database"
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            DB_EXECUTOR.execute(() -> {
                                try {
                                    AppDatabase dbInstance = getInstance(context);
                                    if (dbInstance != null) {
                                        populateInitialData(dbInstance);
                                    }
                                } catch (Exception ignored) {}
                            });
                        }
                    })
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Seeds initial sensible defaults on first database creation.
     */
    private static void populateInitialData(AppDatabase database) {
        // 1. Default Weekday Schedule (11:00 PM -> 07:00 AM)
        SleepSchedule weekday = new SleepSchedule();
        weekday.setName("Weekday Routine");
        weekday.setSleepHour(23);
        weekday.setSleepMinute(0);
        weekday.setWakeHour(7);
        weekday.setWakeMinute(0);
        weekday.setDaysOfWeek(TimeUtils.WEEKDAYS);
        weekday.setEnabled(true);
        weekday.setChallengeType(Constants.CHALLENGE_RANDOM);
        weekday.setChallengeDifficulty(Constants.DIFFICULTY_MEDIUM);
        database.sleepScheduleDao().insert(weekday);

        // 2. Default Weekend Schedule (12:00 AM -> 08:30 AM)
        SleepSchedule weekend = new SleepSchedule();
        weekend.setName("Weekend Sleep");
        weekend.setSleepHour(0);
        weekend.setSleepMinute(0);
        weekend.setWakeHour(8);
        weekend.setWakeMinute(30);
        weekend.setDaysOfWeek(TimeUtils.WEEKENDS);
        weekend.setEnabled(true);
        weekend.setChallengeType(Constants.CHALLENGE_RANDOM);
        weekend.setChallengeDifficulty(Constants.DIFFICULTY_MEDIUM);
        database.sleepScheduleDao().insert(weekend);

        // 3. Default Essential Whitelist Entries
        database.whitelistDao().insert(new WhitelistEntry("com.google.android.dialer", Constants.WHITELIST_TYPE_APP, "Phone", true));
        database.whitelistDao().insert(new WhitelistEntry("com.android.dialer", Constants.WHITELIST_TYPE_APP, "Phone", true));
        database.whitelistDao().insert(new WhitelistEntry("com.android.phone", Constants.WHITELIST_TYPE_APP, "Phone", true));
        database.whitelistDao().insert(new WhitelistEntry("com.google.android.deskclock", Constants.WHITELIST_TYPE_APP, "Clock", true));
        database.whitelistDao().insert(new WhitelistEntry("com.android.deskclock", Constants.WHITELIST_TYPE_APP, "Clock", true));
        database.whitelistDao().insert(new WhitelistEntry("com.google.android.apps.messaging", Constants.WHITELIST_TYPE_APP, "Messages", false));
        database.whitelistDao().insert(new WhitelistEntry("com.google.android.apps.maps", Constants.WHITELIST_TYPE_APP, "Maps", false));

        // 4. Emergency Contacts Numbers
        database.whitelistDao().insert(new WhitelistEntry("911", Constants.WHITELIST_TYPE_CONTACT, "Emergency 911", true));
        database.whitelistDao().insert(new WhitelistEntry("112", Constants.WHITELIST_TYPE_CONTACT, "Emergency 112", true));
        database.whitelistDao().insert(new WhitelistEntry("999", Constants.WHITELIST_TYPE_CONTACT, "Emergency 999", true));
    }
}
