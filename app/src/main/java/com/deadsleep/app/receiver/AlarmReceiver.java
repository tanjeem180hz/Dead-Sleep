package com.deadsleep.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.deadsleep.app.manager.DeadSleepAlarmManager;
import com.deadsleep.app.util.Constants;

/**
 * Fired by AlarmManager at wake-up time.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            long scheduleId = -1;
            String scheduleName = "Dead Sleep";

            if (intent != null) {
                scheduleId = intent.getLongExtra(Constants.EXTRA_SCHEDULE_ID, -1);
                scheduleName = intent.getStringExtra(Constants.EXTRA_SCHEDULE_NAME);
                if (scheduleName == null) scheduleName = "Wake Up Alarm";
            }

            DeadSleepAlarmManager.getInstance(context).startAlarm(scheduleId, scheduleName);
        } catch (Exception ignored) {}
    }
}
