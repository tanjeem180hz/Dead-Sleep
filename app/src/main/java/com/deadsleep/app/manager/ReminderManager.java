package com.deadsleep.app.manager;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.deadsleep.app.data.PreferencesManager;
import com.deadsleep.app.receiver.NudgeReceiver;
import com.deadsleep.app.util.Constants;
import com.deadsleep.app.util.NotificationHelper;

/**
 * Handles scheduling, snoozing, and dispatching bedtime reminders.
 */
public class ReminderManager {

    private static volatile ReminderManager INSTANCE;
    private final Context context;
    private final PreferencesManager preferencesManager;
    private final NotificationManager notificationManager;
    private final AlarmManager alarmManager;

    private ReminderManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferencesManager = PreferencesManager.getInstance(this.context);
        this.notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
    }

    public static ReminderManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ReminderManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ReminderManager(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Dispatches the 30-min (or configured lead) bedtime reminder.
     */
    public void showBedtimeNudge(boolean isFinal) {
        if (preferencesManager.isSleepModeActive()) return;

        int leadMinutes = isFinal ? 5 : preferencesManager.getNudgeLeadMinutes();
        String title = isFinal ? "Bedtime in 5 minutes! 🌙" : "Time to wind down 🌙";
        String body = isFinal
                ? "Sleep mode begins in 5 minutes. Put your phone away and prepare for rest."
                : "Sleep mode starts in " + leadMinutes + " minutes. Finish up and get ready for sleep.";

        int notifId = isFinal ? Constants.NOTIF_ID_NUDGE_FINAL : Constants.NOTIF_ID_NUDGE;
        if (notificationManager != null) {
            try {
                notificationManager.notify(
                        notifId,
                        NotificationHelper.buildNudgeNotification(context, title, body, leadMinutes)
                );
            } catch (Exception ignored) {}
        }
    }

    /**
     * Snoozes the bedtime reminder by 10 minutes.
     */
    public void snoozeNudge(int minutes) {
        if (notificationManager != null) {
            notificationManager.cancel(Constants.NOTIF_ID_NUDGE);
            notificationManager.cancel(Constants.NOTIF_ID_NUDGE_FINAL);
        }

        long triggerAt = System.currentTimeMillis() + (minutes * 60 * 1000L);
        Intent intent = new Intent(context, NudgeReceiver.class);
        intent.putExtra("is_final_nudge", false);

        PendingIntent pi = PendingIntent.getBroadcast(
                context, Constants.NOTIF_ID_NUDGE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
                }
            } catch (Exception e) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }
        }
    }
}
