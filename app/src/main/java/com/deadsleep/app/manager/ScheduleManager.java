package com.deadsleep.app.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.deadsleep.app.data.AppDatabase;
import com.deadsleep.app.data.DeadSleepRepository;
import com.deadsleep.app.data.PreferencesManager;
import com.deadsleep.app.model.SleepSchedule;
import com.deadsleep.app.receiver.AlarmReceiver;
import com.deadsleep.app.receiver.NudgeReceiver;
import com.deadsleep.app.receiver.SleepStartReceiver;
import com.deadsleep.app.ui.MainActivity;
import com.deadsleep.app.util.Constants;
import com.deadsleep.app.util.TimeUtils;

import java.util.Calendar;
import java.util.List;

/**
 * Single source of truth for scheduling all alarms, bedtime nudges, and sleep starts.
 */
public class ScheduleManager {

    private static volatile ScheduleManager INSTANCE;
    private final Context context;
    private final DeadSleepRepository repository;
    private final PreferencesManager preferencesManager;
    private final AlarmManager alarmManager;

    private ScheduleManager(Context context) {
        this.context = context.getApplicationContext();
        this.repository = DeadSleepRepository.getInstance(this.context);
        this.preferencesManager = PreferencesManager.getInstance(this.context);
        this.alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
    }

    public static ScheduleManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ScheduleManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ScheduleManager(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Reschedules all upcoming events based on currently enabled schedules.
     */
    public void rescheduleAll() {
        AppDatabase.getExecutor().execute(() -> {
            List<SleepSchedule> enabledSchedules = repository.getEnabledSchedulesSync();
            if (enabledSchedules.isEmpty()) {
                cancelAllAlarms();
                return;
            }

            SleepSchedule nextSchedule = findNextActiveSchedule(enabledSchedules);
            if (nextSchedule == null) {
                cancelAllAlarms();
                return;
            }

            preferencesManager.setCurrentScheduleId(nextSchedule.getId());

            long nextSleepTime = TimeUtils.getNextOccurrenceMillis(
                    nextSchedule.getSleepHour(), nextSchedule.getSleepMinute(), nextSchedule.getDaysOfWeek()
            );

            // Compute corresponding wake time
            Calendar wakeCal = Calendar.getInstance();
            wakeCal.setTimeInMillis(nextSleepTime);
            wakeCal.set(Calendar.HOUR_OF_DAY, nextSchedule.getWakeHour());
            wakeCal.set(Calendar.MINUTE, nextSchedule.getWakeMinute());
            wakeCal.set(Calendar.SECOND, 0);
            if (wakeCal.getTimeInMillis() <= nextSleepTime) {
                // Crosses midnight: wake time is next morning
                wakeCal.add(Calendar.DAY_OF_YEAR, 1);
            }
            long nextWakeTime = wakeCal.getTimeInMillis();

            // 1. Schedule Bedtime Nudges
            int leadMinutes = preferencesManager.getNudgeLeadMinutes();
            long nudgeTime = nextSleepTime - (leadMinutes * 60 * 1000L);
            if (nudgeTime > System.currentTimeMillis()) {
                scheduleNudge(nudgeTime, Constants.NOTIF_ID_NUDGE, false);
            }

            if (preferencesManager.isSecondNudgeEnabled()) {
                long secondNudgeTime = nextSleepTime - (5 * 60 * 1000L);
                if (secondNudgeTime > System.currentTimeMillis()) {
                    scheduleNudge(secondNudgeTime, Constants.NOTIF_ID_NUDGE_FINAL, true);
                }
            }

            // 2. Schedule Sleep Mode Start
            scheduleSleepStart(nextSleepTime, nextSchedule.getId());

            // 3. Schedule Wake Alarm
            scheduleWakeAlarm(nextWakeTime, nextSchedule);
        });
    }

    private SleepSchedule findNextActiveSchedule(List<SleepSchedule> schedules) {
        SleepSchedule best = null;
        long closest = Long.MAX_VALUE;
        long now = System.currentTimeMillis();

        for (SleepSchedule schedule : schedules) {
            long sleepTime = TimeUtils.getNextOccurrenceMillis(
                    schedule.getSleepHour(), schedule.getSleepMinute(), schedule.getDaysOfWeek()
            );
            if (sleepTime > now && sleepTime < closest) {
                closest = sleepTime;
                best = schedule;
            }
        }
        return best != null ? best : (schedules.isEmpty() ? null : schedules.get(0));
    }

    private void scheduleNudge(long triggerAtMillis, int requestCode, boolean isFinal) {
        Intent intent = new Intent(context, NudgeReceiver.class);
        intent.putExtra("is_final_nudge", isFinal);
        PendingIntent pi = PendingIntent.getBroadcast(
                context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        setExactAlarm(triggerAtMillis, pi);
    }

    private void scheduleSleepStart(long triggerAtMillis, long scheduleId) {
        Intent intent = new Intent(context, SleepStartReceiver.class);
        intent.putExtra(Constants.EXTRA_SCHEDULE_ID, scheduleId);
        PendingIntent pi = PendingIntent.getBroadcast(
                context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        setExactAlarm(triggerAtMillis, pi);
    }

    public void scheduleWakeAlarm(long triggerAtMillis, SleepSchedule schedule) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Constants.EXTRA_SCHEDULE_ID, schedule.getId());
        intent.putExtra(Constants.EXTRA_SCHEDULE_NAME, schedule.getName());
        PendingIntent pi = PendingIntent.getBroadcast(
                context, 200, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Intent showIntent = new Intent(context, MainActivity.class);
                    PendingIntent showPI = PendingIntent.getActivity(
                            context, 0, showIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );
                    AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(triggerAtMillis, showPI);
                    alarmManager.setAlarmClock(info, pi);
                } else {
                    setExactAlarm(triggerAtMillis, pi);
                }
            } catch (Exception e) {
                setExactAlarm(triggerAtMillis, pi);
            }
        }
    }

    private void setExactAlarm(long triggerAtMillis, PendingIntent pi) {
        if (alarmManager == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
            }
        } catch (SecurityException ignored) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        }
    }

    public void cancelAllAlarms() {
        if (alarmManager == null) return;

        PendingIntent nudgePI = PendingIntent.getBroadcast(
                context, Constants.NOTIF_ID_NUDGE, new Intent(context, NudgeReceiver.class),
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (nudgePI != null) alarmManager.cancel(nudgePI);

        PendingIntent sleepPI = PendingIntent.getBroadcast(
                context, 100, new Intent(context, SleepStartReceiver.class),
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (sleepPI != null) alarmManager.cancel(sleepPI);

        PendingIntent alarmPI = PendingIntent.getBroadcast(
                context, 200, new Intent(context, AlarmReceiver.class),
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmPI != null) alarmManager.cancel(alarmPI);
    }

    /**
     * Computes remaining milliseconds until next sleep mode starts for countdown UI.
     */
    public long getMillisUntilNextSleep() {
        try {
            List<SleepSchedule> enabled = repository.getEnabledSchedulesSync();
            if (enabled == null || enabled.isEmpty()) return -1;

            SleepSchedule next = findNextActiveSchedule(enabled);
            if (next == null) return -1;

            long nextSleep = TimeUtils.getNextOccurrenceMillis(
                    next.getSleepHour(), next.getSleepMinute(), next.getDaysOfWeek()
            );
            return Math.max(0, nextSleep - System.currentTimeMillis());
        } catch (Exception e) {
            return -1;
        }
    }
}
