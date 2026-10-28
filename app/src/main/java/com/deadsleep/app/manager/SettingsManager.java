package com.deadsleep.app.manager;

import android.content.Context;

import com.deadsleep.app.data.AppDatabase;
import com.deadsleep.app.data.DeadSleepRepository;
import com.deadsleep.app.data.PreferencesManager;
import com.deadsleep.app.model.BlockedAttempt;
import com.deadsleep.app.model.ChallengeResult;
import com.deadsleep.app.model.SleepSchedule;
import com.deadsleep.app.model.SleepSession;
import com.deadsleep.app.util.TimeUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Handles application settings export, JSON/CSV statistics dumping, backup, and data resets.
 */
public class SettingsManager {

    private static volatile SettingsManager INSTANCE;
    private final Context context;
    private final DeadSleepRepository repository;
    private final PreferencesManager preferencesManager;

    private SettingsManager(Context context) {
        this.context = context.getApplicationContext();
        this.repository = DeadSleepRepository.getInstance(this.context);
        this.preferencesManager = PreferencesManager.getInstance(this.context);
    }

    public static SettingsManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SettingsManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SettingsManager(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Exports sleep history as a CSV file in the app files directory.
     */
    public File exportStatisticsCSV() throws IOException {
        List<SleepSession> sessions = repository.getRecentSessionsSync(100);
        File exportDir = new File(context.getFilesDir(), "exports");
        exportDir.mkdirs();
        File file = new File(exportDir, "dead_sleep_stats_" + System.currentTimeMillis() + ".csv");

        StringBuilder sb = new StringBuilder();
        sb.append("SessionID,ScheduleName,StartTime,EndTime,DurationMinutes,Snoozes,BlockedAttempts,ChallengeType,Status\n");

        for (SleepSession s : sessions) {
            sb.append(s.getId()).append(",")
                    .append("\"").append(s.getScheduleName()).append("\",")
                    .append(TimeUtils.formatDateTime(s.getStartTimeMillis())).append(",")
                    .append(s.getEndTimeMillis() > 0 ? TimeUtils.formatDateTime(s.getEndTimeMillis()) : "N/A").append(",")
                    .append(s.getActualDurationMinutes()).append(",")
                    .append(s.getSnoozesUsed()).append(",")
                    .append(s.getBlockedAttemptsCount()).append(",")
                    .append(s.getChallengeType() != null ? s.getChallengeType() : "N/A").append(",")
                    .append(s.getStatus()).append("\n");
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(sb.toString().getBytes());
        }
        return file;
    }

    /**
     * Exports full settings and schedules as a JSON backup file.
     */
    public File exportBackupJSON() throws Exception {
        JSONObject root = new JSONObject();
        root.put("version", 1);
        root.put("exportedAt", System.currentTimeMillis());

        JSONObject settings = new JSONObject();
        settings.put("nudgeLeadMinutes", preferencesManager.getNudgeLeadMinutes());
        settings.put("secondNudgeEnabled", preferencesManager.isSecondNudgeEnabled());
        settings.put("defaultDifficulty", preferencesManager.getDefaultDifficulty());
        settings.put("defaultChallenge", preferencesManager.getDefaultChallenge());
        settings.put("userName", preferencesManager.getUserName());
        root.put("settings", settings);

        JSONArray schedulesArray = new JSONArray();
        List<SleepSchedule> schedules = repository.getAllSchedulesSync();
        for (SleepSchedule s : schedules) {
            JSONObject obj = new JSONObject();
            obj.put("name", s.getName());
            obj.put("sleepHour", s.getSleepHour());
            obj.put("sleepMinute", s.getSleepMinute());
            obj.put("wakeHour", s.getWakeHour());
            obj.put("wakeMinute", s.getWakeMinute());
            obj.put("daysOfWeek", s.getDaysOfWeek());
            obj.put("isEnabled", s.isEnabled());
            obj.put("challengeType", s.getChallengeType());
            obj.put("challengeDifficulty", s.getChallengeDifficulty());
            schedulesArray.put(obj);
        }
        root.put("schedules", schedulesArray);

        File exportDir = new File(context.getFilesDir(), "backups");
        exportDir.mkdirs();
        File file = new File(exportDir, "dead_sleep_backup_" + System.currentTimeMillis() + ".json");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(root.toString(2).getBytes());
        }
        return file;
    }

    /**
     * Resets all application data, schedules, statistics, and preferences.
     */
    public void resetAllData() {
        AppDatabase.getExecutor().execute(() -> {
            ScheduleManager.getInstance(context).cancelAllAlarms();
            preferencesManager.resetAll();
            AppDatabase.getInstance(context).clearAllTables();
            // Reseed default data
            ScheduleManager.getInstance(context).rescheduleAll();
        });
    }
}
