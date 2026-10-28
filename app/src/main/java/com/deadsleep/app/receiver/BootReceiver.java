package com.deadsleep.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.deadsleep.app.data.AppDatabase;
import com.deadsleep.app.data.PreferencesManager;
import com.deadsleep.app.manager.ScheduleManager;
import com.deadsleep.app.manager.SleepModeManager;

/**
 * Handles device reboot, timezone shifts, manual clock adjustments, and app updates.
 * Guarantees schedules and active sleep states survive reboots without interruption.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)) {

            AppDatabase.getExecutor().execute(() -> {
                PreferencesManager prefs = PreferencesManager.getInstance(context);

                // If sleep mode was active when device shut down, restore active state immediately
                if (prefs.isSleepModeActive()) {
                    long activeScheduleId = prefs.getCurrentScheduleId();
                    SleepModeManager.getInstance(context).startSleepMode(activeScheduleId);
                }

                // Reschedule all upcoming events
                ScheduleManager.getInstance(context).rescheduleAll();
            });
        }
    }
}
