package com.deadsleep.app.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.deadsleep.app.data.AppDatabase;
import com.deadsleep.app.data.DeadSleepRepository;
import com.deadsleep.app.model.SleepSchedule;
import com.deadsleep.app.util.Constants;
import com.deadsleep.app.util.NotificationHelper;

/**
 * Ongoing foreground service running throughout the duration of Sleep Mode.
 */
public class SleepModeForegroundService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long scheduleId = -1;
        if (intent != null) {
            scheduleId = intent.getLongExtra(Constants.EXTRA_SCHEDULE_ID, -1);
        }

        // 1. Immediately promote to foreground to satisfy the Android 5-second startForeground contract
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= 34) {
                    startForeground(
                            Constants.NOTIF_ID_SLEEP_MODE,
                            NotificationHelper.buildSleepModeNotification(this, "Morning"),
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    );
                } else {
                    startForeground(
                            Constants.NOTIF_ID_SLEEP_MODE,
                            NotificationHelper.buildSleepModeNotification(this, "Morning"),
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                    );
                }
            } else {
                startForeground(
                        Constants.NOTIF_ID_SLEEP_MODE,
                        NotificationHelper.buildSleepModeNotification(this, "Morning")
                );
            }
        } catch (Exception ignored) {}

        // 2. Asynchronously retrieve the exact wake time and update notification
        final long targetScheduleId = scheduleId;
        AppDatabase.getExecutor().execute(() -> {
            try {
                String wakeTime = "Morning";
                if (targetScheduleId != -1) {
                    SleepSchedule schedule = DeadSleepRepository.getInstance(this).getScheduleById(targetScheduleId);
                    if (schedule != null) {
                        wakeTime = schedule.getFormattedWakeTime();
                    }
                }
                android.app.NotificationManager nm = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (nm != null) {
                    nm.notify(
                            Constants.NOTIF_ID_SLEEP_MODE,
                            NotificationHelper.buildSleepModeNotification(this, wakeTime)
                    );
                }
            } catch (Exception ignored) {}
        });

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
