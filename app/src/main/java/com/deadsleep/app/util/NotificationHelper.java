package com.deadsleep.app.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.deadsleep.app.R;
import com.deadsleep.app.receiver.NudgeReceiver;
import com.deadsleep.app.receiver.SleepStartReceiver;
import com.deadsleep.app.ui.AlarmRingingActivity;
import com.deadsleep.app.ui.MainActivity;
import com.deadsleep.app.ui.PermissionWizardActivity;
import com.deadsleep.app.ui.SleepModeActiveActivity;

/**
 * Creates notification channels and builds rich, actionable notifications.
 */
public final class NotificationHelper {

    private NotificationHelper() {}

    /**
     * Initializes all 4 notification channels on Android 8.0+.
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm == null) return;

        // 1. Bedtime Nudges Channel
        NotificationChannel nudgeChannel = new NotificationChannel(
                Constants.CHANNEL_ID_NUDGE,
                Constants.CHANNEL_NAME_NUDGE,
                NotificationManager.IMPORTANCE_HIGH
        );
        nudgeChannel.setDescription("Bedtime reminders and wind-down nudges");
        nudgeChannel.enableVibration(true);
        nm.createNotificationChannel(nudgeChannel);

        // 2. Sleep Mode Ongoing Channel
        NotificationChannel sleepChannel = new NotificationChannel(
                Constants.CHANNEL_ID_SLEEP_MODE,
                Constants.CHANNEL_NAME_SLEEP_MODE,
                NotificationManager.IMPORTANCE_LOW
        );
        sleepChannel.setDescription("Persistent status indicator while sleep mode is active");
        sleepChannel.setShowBadge(false);
        nm.createNotificationChannel(sleepChannel);

        // 3. Alarm Channel (High priority, heads-up)
        NotificationChannel alarmChannel = new NotificationChannel(
                Constants.CHANNEL_ID_ALARM,
                Constants.CHANNEL_NAME_ALARM,
                NotificationManager.IMPORTANCE_HIGH
        );
        alarmChannel.setDescription("Wake-up alarms that require a challenge to dismiss");
        alarmChannel.setBypassDnd(true);
        alarmChannel.enableVibration(true);
        alarmChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        nm.createNotificationChannel(alarmChannel);

        // 4. Stats & Streaks Channel
        NotificationChannel statsChannel = new NotificationChannel(
                Constants.CHANNEL_ID_STATS,
                Constants.CHANNEL_NAME_STATS,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        statsChannel.setDescription("Weekly sleep consistency insights and streak updates");
        nm.createNotificationChannel(statsChannel);
    }

    /**
     * Builds the Bedtime Nudge notification with "Start sleep now" and "Snooze 10 min" actions.
     */
    public static Notification buildNudgeNotification(Context context, String title, String body, int leadMinutes) {
        createNotificationChannels(context);

        Intent openAppIntent = new Intent(context, MainActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Action: Start Sleep Now
        Intent startSleepIntent = new Intent(context, SleepStartReceiver.class);
        startSleepIntent.setAction(Constants.ACTION_START_SLEEP_NOW);
        PendingIntent startSleepPI = PendingIntent.getBroadcast(
                context, 1, startSleepIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Action: Snooze 10 min
        Intent snoozeIntent = new Intent(context, NudgeReceiver.class);
        snoozeIntent.setAction(Constants.ACTION_SNOOZE_NUDGE);
        PendingIntent snoozePI = PendingIntent.getBroadcast(
                context, 2, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(context, Constants.CHANNEL_ID_NUDGE)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(contentPendingIntent)
                .setAutoCancel(true)
                .addAction(0, "🌙 Start Sleep Now", startSleepPI)
                .addAction(0, "⏰ Snooze 10m", snoozePI)
                .build();
    }

    /**
     * Builds the ongoing Sleep Mode Foreground Service notification.
     */
    public static Notification buildSleepModeNotification(Context context, String wakeTimeDisplay) {
        createNotificationChannels(context);

        Intent sleepIntent = new Intent(context, SleepModeActiveActivity.class);
        sleepIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 10, sleepIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String message = "Sleep mode active until " + wakeTimeDisplay + ". Distracting apps are locked.";

        return new NotificationCompat.Builder(context, Constants.CHANNEL_ID_SLEEP_MODE)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Dead Sleep Active 🌙")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent)
                .build();
    }

    /**
     * Builds the Alarm Foreground Service notification with fullScreenIntent.
     */
    public static Notification buildAlarmNotification(Context context, long scheduleId, String scheduleLabel) {
        createNotificationChannels(context);

        Intent ringingIntent = new Intent(context, AlarmRingingActivity.class);
        ringingIntent.putExtra(Constants.EXTRA_SCHEDULE_ID, scheduleId);
        ringingIntent.putExtra(Constants.EXTRA_SCHEDULE_NAME, scheduleLabel);
        ringingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);

        PendingIntent fullScreenPI = PendingIntent.getActivity(
                context, 20, ringingIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(context, Constants.CHANNEL_ID_ALARM)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Alarm: " + (scheduleLabel != null ? scheduleLabel : "Dead Sleep"))
                .setContentText("Solve the challenge to wake up!")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setFullScreenIntent(fullScreenPI, true)
                .setContentIntent(fullScreenPI)
                .setOngoing(true)
                .setAutoCancel(false)
                .build();
    }

    /**
     * Builds warning notification when accessibility or critical permissions are revoked.
     */
    public static Notification buildPermissionWarningNotification(Context context, String missingPermissionName) {
        createNotificationChannels(context);

        Intent fixIntent = new Intent(context, PermissionWizardActivity.class);
        PendingIntent pi = PendingIntent.getActivity(
                context, 30, fixIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(context, Constants.CHANNEL_ID_NUDGE)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Protection Alert ⚠️")
                .setContentText(missingPermissionName + " is disabled. Tap to re-enable protection.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();
    }
}
