package com.deadsleep.app.manager;

import android.content.Context;

import com.deadsleep.app.data.DeadSleepRepository;
import com.deadsleep.app.model.BlockedAttempt;
import com.deadsleep.app.model.RoutineSuggestion;
import com.deadsleep.app.model.SleepSchedule;
import com.deadsleep.app.model.SleepSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Intelligent rule-based routine advisor analyzing sleep trends and providing 1-tap improvements.
 */
public class RoutineAdvisor {

    private static volatile RoutineAdvisor INSTANCE;
    private final Context context;
    private final DeadSleepRepository repository;

    private RoutineAdvisor(Context context) {
        this.context = context.getApplicationContext();
        this.repository = DeadSleepRepository.getInstance(this.context);
    }

    public static RoutineAdvisor getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (RoutineAdvisor.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RoutineAdvisor(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Evaluates data history and returns personalized routine recommendations.
     */
    public List<RoutineSuggestion> generateRecommendations() {
        List<RoutineSuggestion> suggestions = new ArrayList<>();
        List<SleepSession> sessions = repository.getRecentSessionsSync(14);

        // Rule 1: High Snooze Usage
        int totalSnoozes = 0;
        int completedCount = 0;
        int totalDurationMin = 0;

        for (SleepSession s : sessions) {
            if (s.isCompleted()) {
                completedCount++;
                totalSnoozes += s.getSnoozesUsed();
                totalDurationMin += s.getActualDurationMinutes();
            }
        }

        if (completedCount > 0 && ((float) totalSnoozes / completedCount) >= 1.5f) {
            suggestions.add(new RoutineSuggestion(
                    "rule_snooze",
                    "High Snooze Frequency Detected",
                    "You average " + String.format("%.1f", (float) totalSnoozes / completedCount) +
                            " snoozes per wake-up. Auto-escalating challenge difficulty can help wake your mind quicker.",
                    "SNOOZE_OVERUSE",
                    "Enable Auto-Escalate"
            ));
        }

        // Rule 2: Chronic Sleep Debt (under 7 hours)
        if (completedCount > 0 && ((float) totalDurationMin / completedCount) < 420) {
            float avgHours = (float) totalDurationMin / (completedCount * 60f);
            suggestions.add(new RoutineSuggestion(
                    "rule_duration",
                    "Sleep Duration Below Target",
                    "You average " + String.format("%.1f", avgHours) +
                            " hours per night (target: 8h). Moving your bedtime 30 minutes earlier will improve daytime alertness.",
                    "SLEEP_DEBT",
                    "Shift Bedtime 30m Earlier"
            ));
        }

        // Rule 3: Most Blocked App Distraction
        String topBlocked = repository.getMostBlockedPackage();
        if (topBlocked != null && !topBlocked.isEmpty()) {
            suggestions.add(new RoutineSuggestion(
                    "rule_distraction",
                    "Frequent Bedtime Distraction",
                    "We noticed frequent attempts to open " + topBlocked + " near bedtime. Setting an earlier 45m wind-down reminder helps detach from screens.",
                    "BLOCKED_SPIKE",
                    "Set 45m Nudge Lead"
            ));
        }

        // Default proactive suggestion if history is small
        if (suggestions.isEmpty()) {
            suggestions.add(new RoutineSuggestion(
                    "rule_consistency",
                    "Gentle Weekend Consistency",
                    "Keeping weekend wake times within 1 hour of weekday wake times prevents Monday morning fatigue.",
                    "BEDTIME_DRIFT",
                    "Align Weekend Wake Time"
            ));
        }

        return suggestions;
    }

    /**
     * 1-Tap Apply: Automatically executes the suggested optimization.
     */
    public void applySuggestion(RoutineSuggestion suggestion) {
        if (suggestion == null) return;

        if ("rule_snooze".equals(suggestion.getId())) {
            List<SleepSchedule> schedules = repository.getAllSchedulesSync();
            for (SleepSchedule s : schedules) {
                s.setAutoEscalateChallenge(true);
                repository.updateSchedule(s);
            }
        } else if ("rule_duration".equals(suggestion.getId())) {
            List<SleepSchedule> schedules = repository.getAllSchedulesSync();
            for (SleepSchedule s : schedules) {
                int newMinute = s.getSleepMinute() - 30;
                int newHour = s.getSleepHour();
                if (newMinute < 0) {
                    newMinute += 60;
                    newHour = (newHour - 1 + 24) % 24;
                }
                s.setSleepHour(newHour);
                s.setSleepMinute(newMinute);
                repository.updateSchedule(s);
            }
            ScheduleManager.getInstance(context).rescheduleAll();
        } else if ("rule_distraction".equals(suggestion.getId())) {
            com.deadsleep.app.data.PreferencesManager.getInstance(context).setNudgeLeadMinutes(45);
            ScheduleManager.getInstance(context).rescheduleAll();
        }

        suggestion.setApplied(true);
    }
}
