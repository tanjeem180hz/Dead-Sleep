package com.deadsleep.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.deadsleep.app.manager.SleepModeManager;
import com.deadsleep.app.util.Constants;

/**
 * Triggered at bedtime to automatically initiate Sleep Mode.
 */
public class SleepStartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        long scheduleId = -1;
        if (intent != null) {
            scheduleId = intent.getLongExtra(Constants.EXTRA_SCHEDULE_ID, -1);
        }
        SleepModeManager.getInstance(context).startSleepMode(scheduleId);
    }
}
