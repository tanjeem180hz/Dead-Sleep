package com.deadsleep.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.deadsleep.app.data.PreferencesManager;
import com.deadsleep.app.manager.ReminderManager;
import com.deadsleep.app.manager.SleepModeManager;
import com.deadsleep.app.util.Constants;

/**
 * Handles bedtime reminders and quick notification actions.
 */
public class NudgeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();

        if (Constants.ACTION_START_SLEEP_NOW.equals(action)) {
            // User clicked "Start Sleep Now" on the notification
            long scheduleId = PreferencesManager.getInstance(context).getCurrentScheduleId();
            SleepModeManager.getInstance(context).startSleepMode(scheduleId);
        } else if (Constants.ACTION_SNOOZE_NUDGE.equals(action)) {
            // User clicked "Snooze 10m"
            ReminderManager.getInstance(context).snoozeNudge(10);
        } else {
            // Standard alarm trigger for nudge
            boolean isFinal = intent.getBooleanExtra("is_final_nudge", false);
            ReminderManager.getInstance(context).showBedtimeNudge(isFinal);
        }
    }
}
