package com.deadsleep.app.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.deadsleep.app.data.AppDatabase;
import com.deadsleep.app.data.DeadSleepRepository;
import com.deadsleep.app.data.PreferencesManager;
import com.deadsleep.app.model.SleepSchedule;
import com.deadsleep.app.model.SleepSession;
import com.deadsleep.app.service.SleepModeForegroundService;
import com.deadsleep.app.util.Constants;
import com.deadsleep.app.util.TimeUtils;

/**
 * Manages sleep mode lifecycle, session persistence, edit locking, and emergency exits.
 */
public class SleepModeManager {

    private static volatile SleepModeManager INSTANCE;
    private final Context context;
    private final DeadSleepRepository repository;
    private final PreferencesManager preferencesManager;

    private SleepModeManager(Context context) {
        this.context = context.getApplicationContext();
        this.repository = DeadSleepRepository.getInstance(this.context);
        this.preferencesManager = PreferencesManager.getInstance(this.context);
    }

    public static SleepModeManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SleepModeManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SleepModeManager(context);
                }
            }
        }
        return INSTANCE;
    }

    public boolean isSleepModeActive() {
        return preferencesManager.isSleepModeActive();
    }

    /**
     * Activates sleep mode: starts foreground service, creates active session, and enables lock.
     */
    public void startSleepMode(long scheduleId) {
        preferencesManager.setSleepModeActive(true);

        AppDatabase.getExecutor().execute(() -> {
            SleepSchedule schedule = repository.getScheduleById(scheduleId);
            if (schedule == null) {
                schedule = ScheduleManager.getInstance(context).getNextActiveSchedule();
            }
            String scheduleName = schedule != null ? schedule.getName() : "Bedtime";
            long activeSchedId = schedule != null ? schedule.getId() : scheduleId;

            SleepSession session = new SleepSession();
            session.setScheduleId(activeSchedId);
            session.setScheduleName(scheduleName);
            session.setStartTimeMillis(System.currentTimeMillis());

            if (schedule != null) {
                long wakeTime = TimeUtils.getTargetWakeTimeMillis(
                        session.getStartTimeMillis(), schedule.getWakeHour(), schedule.getWakeMinute()
                );
                session.setTargetWakeTimeMillis(wakeTime);
                session.setChallengeType(schedule.getChallengeType());
                preferencesManager.setCurrentScheduleId(schedule.getId());
            }

            long sessionId = repository.insertSessionSync(session);
            preferencesManager.setActiveSessionId(sessionId);

            // Start SleepModeForegroundService
            Intent serviceIntent = new Intent(context, SleepModeForegroundService.class);
            serviceIntent.putExtra(Constants.EXTRA_SESSION_ID, sessionId);
            serviceIntent.putExtra(Constants.EXTRA_SCHEDULE_ID, activeSchedId);

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
            } catch (Exception ignored) {}
        });
    }

    /**
     * Ends sleep mode normally upon solving wake challenge.
     */
    public void endSleepMode(boolean challengeSolved, int challengeTimeSec) {
        preferencesManager.setSleepModeActive(false);

        AppDatabase.getExecutor().execute(() -> {
            long sessionId = preferencesManager.getActiveSessionId();
            SleepSession session = repository.getActiveSessionSync();

            if (session != null) {
                session.setEndTimeMillis(System.currentTimeMillis());
                session.setActualWakeTimeMillis(System.currentTimeMillis());
                session.setCompleted(challengeSolved);
                session.setStatus(challengeSolved ? "COMPLETED" : "DISMISSED");
                session.setChallengeSolveTimeSec(challengeTimeSec);
                repository.updateSessionSync(session);
            }

            preferencesManager.setActiveSessionId(-1);

            // Update user streak
            StatsManager.getInstance(context).updateStreakOnSessionComplete();

            // Stop service
            context.stopService(new Intent(context, SleepModeForegroundService.class));

            // Reschedule next cycle
            ScheduleManager.getInstance(context).rescheduleAll();
        });
    }

    /**
     * Emergency exit: ends sleep mode immediately and logs the emergency unlock event.
     */
    public void emergencyUnlock() {
        preferencesManager.setSleepModeActive(false);

        AppDatabase.getExecutor().execute(() -> {
            SleepSession session = repository.getActiveSessionSync();
            if (session != null) {
                session.setEndTimeMillis(System.currentTimeMillis());
                session.setActualWakeTimeMillis(System.currentTimeMillis());
                session.setCompleted(false);
                session.setStatus("EMERGENCY_UNLOCKED");
                repository.updateSessionSync(session);
            }

            preferencesManager.setActiveSessionId(-1);
            context.stopService(new Intent(context, SleepModeForegroundService.class));
            ScheduleManager.getInstance(context).rescheduleAll();
        });
    }

    /**
     * Checks if schedule editing is currently allowed (blocked during active sleep mode).
     */
    public boolean canEditSchedules() {
        return !preferencesManager.isSleepModeActive();
    }
}
