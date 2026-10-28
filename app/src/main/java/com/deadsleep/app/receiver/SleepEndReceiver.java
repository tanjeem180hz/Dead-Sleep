package com.deadsleep.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.deadsleep.app.manager.SleepModeManager;

/**
 * Broadcast receiver for ending sleep mode.
 */
public class SleepEndReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SleepModeManager.getInstance(context).endSleepMode(true, 0);
    }
}
