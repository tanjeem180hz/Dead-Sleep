package com.deadsleep.app.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.deadsleep.app.data.PreferencesManager;

/**
 * Device Administrator implementation providing tamper protection during active sleep hours.
 */
public class DeadSleepDeviceAdminReceiver extends DeviceAdminReceiver {

    @Nullable
    @Override
    public CharSequence onDisableRequested(@NonNull Context context, @NonNull Intent intent) {
        PreferencesManager prefs = PreferencesManager.getInstance(context);

        // Block deactivation while sleep mode is active!
        if (prefs.isSleepModeActive()) {
            Toast.makeText(context, "🛡️ Tamper Protection: Device Admin cannot be disabled during Sleep Mode!", Toast.LENGTH_LONG).show();
            return "Dead Sleep is actively protecting your sleep routine! Device Administrator cannot be removed until morning wake-up.";
        }

        return "Are you sure you want to disable Dead Sleep tamper protection?";
    }

    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        super.onEnabled(context, intent);
        Toast.makeText(context, "🛡️ Dead Sleep Tamper Protection enabled.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisabled(@NonNull Context context, @NonNull Intent intent) {
        super.onDisabled(context, intent);
        Toast.makeText(context, "Tamper Protection disabled.", Toast.LENGTH_SHORT).show();
    }
}
