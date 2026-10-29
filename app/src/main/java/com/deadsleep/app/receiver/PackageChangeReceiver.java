package com.deadsleep.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.deadsleep.app.manager.RestrictionManager;

/**
 * Monitors package installations and removals to keep restricted lists in sync.
 */
public class PackageChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getData() == null) return;
        String action = intent.getAction();

        try {
            if (Intent.ACTION_PACKAGE_ADDED.equals(action)
                    || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                    || Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(action)) {
                // Re-sync apps
                RestrictionManager.getInstance(context).scanAndSeedInstalledApps();
            }
        } catch (Exception ignored) {}
    }
}
