package com.deadsleep.app.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;

import com.deadsleep.app.data.AppDatabase;
import com.deadsleep.app.data.DeadSleepRepository;
import com.deadsleep.app.data.PreferencesManager;
import com.deadsleep.app.model.SleepSchedule;
import com.deadsleep.app.model.SleepSession;
import com.deadsleep.app.receiver.AlarmReceiver;
import com.deadsleep.app.service.AlarmForegroundService;
import com.deadsleep.app.util.Constants;

/**
 * Manages alarm execution, snooze escalation, volume enforcement, and test alarms.
 */
public class DeadSleepAlarmManager {

    private static volatile DeadSleepAlarmManager INSTANCE;
    private final Context context;
    private final DeadSleepRepository repository;
    private final PreferencesManager preferencesManager;
    private final AlarmManager alarmManager;
    private final AudioManager audioManager;

    private int currentSnoozeCount = 0;
    private long currentAlarmScheduleId = -1;

    private DeadSleepAlarmManager(Context context) {
        this.context = context.getApplicationContext();
        this.repository = DeadSleepRepository.getInstance(this.context);
        this.preferencesManager = PreferencesManager.getInstance(this.context);
        this.alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
        this.audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
    }

    public static DeadSleepAlarmManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DeadSleepAlarmManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DeadSleepAlarmManager(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Called when the alarm receiver fires at wake-up time.
     */
    public void startAlarm(long scheduleId, String scheduleLabel) {
        currentAlarmScheduleId = scheduleId;

        Intent serviceIntent = new Intent(context, AlarmForegroundService.class);
        serviceIntent.putExtra(Constants.EXTRA_SCHEDULE_ID, scheduleId);
        serviceIntent.putExtra(Constants.EXTRA_SCHEDULE_NAME, scheduleLabel);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        } catch (Exception e) {
            try {
                Intent ringActivity = new Intent(context, com.deadsleep.app.ui.AlarmRingingActivity.class);
                ringActivity.putExtra(Constants.EXTRA_SCHEDULE_ID, scheduleId);
                ringActivity.putExtra(Constants.EXTRA_SCHEDULE_NAME, scheduleLabel);
                ringActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(ringActivity);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Snoozes the alarm if max snooze count is not exceeded.
     */
    public boolean snoozeAlarm(long scheduleId, int snoozeMinutes, int maxSnoozes) {
        if (currentSnoozeCount >= maxSnoozes) {
            return false;
        }

        currentSnoozeCount++;

        // Increment snoozes used in active session
        AppDatabase.getExecutor().execute(() -> {
            SleepSession session = repository.getActiveSessionSync();
            if (session != null) {
                session.setSnoozesUsed(session.getSnoozesUsed() + 1);
                repository.updateSessionSync(session);
            }
        });

        // Stop current ringing
        stopAlarmSound();

        // Schedule next ring
        long snoozeTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Constants.EXTRA_SCHEDULE_ID, scheduleId);
        intent.putExtra(Constants.EXTRA_SCHEDULE_NAME, "Snoozed Alarm");

        PendingIntent pi = PendingIntent.getBroadcast(
                context, 201, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pi);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTime, pi);
            }
        }

        return true;
    }

    public int getCurrentSnoozeCount() {
        return currentSnoozeCount;
    }

    public void resetSnoozeCount() {
        currentSnoozeCount = 0;
    }

    /**
     * Stops the alarm ringing foreground service.
     */
    public void stopAlarmSound() {
        Intent serviceIntent = new Intent(context, AlarmForegroundService.class);
        context.stopService(serviceIntent);
    }

    /**
     * Solves the challenge and completely stops the alarm and finishes sleep mode.
     */
    public void dismissAlarm(int solveTimeSec) {
        stopAlarmSound();
        resetSnoozeCount();
        SleepModeManager.getInstance(context).endSleepMode(true, solveTimeSec);
    }

    /**
     * Triggers a 10-second test alarm so the user can verify sound, vibration, and challenge.
     */
    public void triggerTestAlarm(long delaySeconds) {
        long triggerAt = System.currentTimeMillis() + (delaySeconds * 1000L);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Constants.EXTRA_SCHEDULE_ID, -1L);
        intent.putExtra(Constants.EXTRA_SCHEDULE_NAME, "Test Alarm");
        intent.putExtra(Constants.EXTRA_IS_TEST_ALARM, true);

        PendingIntent pi = PendingIntent.getBroadcast(
                context, 999, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }
        }
    }
}
