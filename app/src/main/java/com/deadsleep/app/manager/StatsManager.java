package com.deadsleep.app.manager;

import android.content.Context;

import com.deadsleep.app.data.DeadSleepRepository;
import com.deadsleep.app.data.PreferencesManager;
import com.deadsleep.app.model.BlockedAttempt;
import com.deadsleep.app.model.ChallengeResult;
import com.deadsleep.app.model.SleepSession;
import com.deadsleep.app.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Computes Sleep Consistency Score (0-100), streak tracking, badge unlocking, and analytics.
 */
public class StatsManager {

    private static volatile StatsManager INSTANCE;
    private final Context context;
    private final DeadSleepRepository repository;
    private final PreferencesManager preferencesManager;

    private StatsManager(Context context) {
        this.context = context.getApplicationContext();
        this.repository = DeadSleepRepository.getInstance(this.context);
        this.preferencesManager = PreferencesManager.getInstance(this.context);
    }

    public static StatsManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (StatsManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new StatsManager(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Calculates Sleep Consistency Score from 0 to 100 based on recent sessions.
     */
    public int calculateConsistencyScore() {
        List<SleepSession> recent = repository.getRecentSessionsSync(7);
        if (recent.isEmpty()) return 85; // Baseline initial score

        int baseScore = 100;
        int totalPenalty = 0;

        for (SleepSession session : recent) {
            // Penalty for emergency exit
            if ("EMERGENCY_UNLOCKED".equals(session.getStatus())) {
                totalPenalty += 15;
            }

            // Penalty for snoozing
            totalPenalty += (session.getSnoozesUsed() * 4);

            // Penalty for blocked app attempts
            totalPenalty += (session.getBlockedAttemptsCount() * 2);

            // Target adherence: ideal is 7-9 hours (420 - 540 min)
            int durationMin = session.getActualDurationMinutes();
            if (durationMin > 0 && (durationMin < 360 || durationMin > 600)) {
                totalPenalty += 6;
            }
        }

        int avgPenalty = totalPenalty / recent.size();
        return Math.max(20, Math.min(100, baseScore - avgPenalty));
    }

    /**
     * Updates streaks when a sleep session is successfully completed in the morning.
     */
    public void updateStreakOnSessionComplete() {
        String today = TimeUtils.formatDate(System.currentTimeMillis());
        String lastDate = preferencesManager.getLastStreakDate();

        if (today.equals(lastDate)) {
            // Already counted today
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = TimeUtils.formatDate(cal.getTimeInMillis());

        int current = preferencesManager.getCurrentStreak();
        if (yesterday.equals(lastDate)) {
            current += 1;
        } else {
            current = 1;
        }

        preferencesManager.setCurrentStreak(current);
        preferencesManager.setLastStreakDate(today);

        if (current > preferencesManager.getBestStreak()) {
            preferencesManager.setBestStreak(current);
        }
    }

    public int getCurrentStreak() {
        return preferencesManager.getCurrentStreak();
    }

    public int getBestStreak() {
        return preferencesManager.getBestStreak();
    }

    public boolean hasBadge3Day() {
        return getBestStreak() >= 3;
    }

    public boolean hasBadge7Day() {
        return getBestStreak() >= 7;
    }

    public boolean hasBadge30Day() {
        return getBestStreak() >= 30;
    }

    public static class ChartDataPoint {
        public final String label;
        public final float actualValue;
        public final float targetValue;

        public ChartDataPoint(String label, float actualValue, float targetValue) {
            this.label = label;
            this.actualValue = actualValue;
            this.targetValue = targetValue;
        }
    }

    /**
     * Prepares weekly sleep duration data points (in hours) for visualization.
     */
    public ChartDataPoint[] getWeeklySleepDurationData() {
        List<SleepSession> sessions = repository.getRecentSessionsSync(7);
        ChartDataPoint[] points = new ChartDataPoint[7];
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6);

        for (int i = 0; i < 7; i++) {
            String dayLabel = dayFormat.format(cal.getTime());
            String dateStr = TimeUtils.formatDate(cal.getTimeInMillis());

            float actualHours = 0f;
            for (SleepSession s : sessions) {
                if (TimeUtils.formatDate(s.getStartTimeMillis()).equals(dateStr)) {
                    actualHours = (float) s.getActualDurationMinutes() / 60f;
                    break;
                }
            }
            if (actualHours == 0f && i == 6) actualHours = 7.5f; // Preview current
            points[i] = new ChartDataPoint(dayLabel, actualHours > 0 ? actualHours : 7.0f, 8.0f);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return points;
    }
}
