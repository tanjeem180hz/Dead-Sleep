package com.deadsleep.app.util;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

import com.deadsleep.app.receiver.DeadSleepDeviceAdminReceiver;
import com.deadsleep.app.service.AppLockAccessibilityService;

import java.util.ArrayList;
import java.util.List;

/**
 * Robust utility for checking, requesting, and guiding the 8 required system permissions.
 */
public final class PermissionUtils {

    private PermissionUtils() {}

    /**
     * 1. POST_NOTIFICATIONS (Android 13+)
     */
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * 2. Usage Access (PACKAGE_USAGE_STATS)
     */
    public static boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) return false;
        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.getPackageName()
        );
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static Intent getUsageStatsIntent() {
        return new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
    }

    /**
     * 3. Accessibility Service (AppLockAccessibilityService)
     */
    public static boolean isAccessibilityServiceEnabled(Context context) {
        ComponentName expectedComponent = new ComponentName(context, AppLockAccessibilityService.class);
        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        if (TextUtils.isEmpty(enabledServices)) return false;

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(enabledServices);
        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledComponent = ComponentName.unflattenFromString(componentNameString);
            if (enabledComponent != null && enabledComponent.equals(expectedComponent)) {
                return true;
            }
        }
        return false;
    }

    public static Intent getAccessibilitySettingsIntent() {
        return new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
    }

    /**
     * 4. Display over other apps (SYSTEM_ALERT_WINDOW)
     */
    public static boolean hasOverlayPermission(Context context) {
        return Settings.canDrawOverlays(context);
    }

    public static Intent getOverlaySettingsIntent(Context context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        return intent;
    }

    /**
     * 5. Exact Alarms (SCHEDULE_EXACT_ALARM)
     */
    public static boolean hasExactAlarmPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    public static Intent getExactAlarmSettingsIntent(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            return intent;
        }
        return new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.getPackageName()));
    }

    /**
     * 6. Device Administrator activation
     */
    public static boolean isDeviceAdminActive(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(context, DeadSleepDeviceAdminReceiver.class);
        return dpm != null && dpm.isAdminActive(adminComponent);
    }

    public static Intent getDeviceAdminRequestIntent(Context context) {
        ComponentName adminComponent = new ComponentName(context, DeadSleepDeviceAdminReceiver.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
        intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Dead Sleep uses Device Administrator to prevent tampering, force-stopping, or uninstalling the app while sleep mode is active."
        );
        return intent;
    }

    /**
     * 7. Ignore Battery Optimizations
     */
    public static boolean isIgnoringBatteryOptimizations(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm != null && pm.isIgnoringBatteryOptimizations(context.getPackageName());
    }

    public static Intent getIgnoreBatteryOptimizationIntent(Context context) {
        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        return intent;
    }

    /**
     * 8. Full-screen intent permission (Android 14+)
     */
    public static boolean hasFullScreenIntentPermission(Context context) {
        if (Build.VERSION.SDK_INT >= 34) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            // On Android 14, USE_FULL_SCREEN_INTENT requires checking if allowed or granted
            return ContextCompat.checkSelfPermission(context, Manifest.permission.USE_FULL_SCREEN_INTENT)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static Intent getFullScreenIntentSettings(Context context) {
        if (Build.VERSION.SDK_INT >= 34) {
            return new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:" + context.getPackageName()));
        }
        return new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.getPackageName()));
    }

    /**
     * Checklist item data class for the Setup Health checklist.
     */
    public static class PermissionItem {
        public final String id;
        public final String title;
        public final String description;
        public final boolean isGranted;
        public final Intent settingsIntent;

        public PermissionItem(String id, String title, String description, boolean isGranted, Intent settingsIntent) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.isGranted = isGranted;
            this.settingsIntent = settingsIntent;
        }
    }

    /**
     * Returns full status list for the Setup Health panel.
     */
    public static List<PermissionItem> getPermissionHealthList(Context context) {
        List<PermissionItem> list = new ArrayList<>();

        list.add(new PermissionItem(
                "notifications",
                "Notifications",
                "Required for bedtime nudges and alarm alerts",
                hasNotificationPermission(context),
                new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName())
        ));

        list.add(new PermissionItem(
                "usage_stats",
                "Usage Access",
                "Detects when distracting apps are opened during bedtime",
                hasUsageStatsPermission(context),
                getUsageStatsIntent()
        ));

        list.add(new PermissionItem(
                "accessibility",
                "Accessibility Service",
                "Instantly locks restricted apps & defends tamper attempts",
                isAccessibilityServiceEnabled(context),
                getAccessibilitySettingsIntent()
        ));

        list.add(new PermissionItem(
                "overlay",
                "Display Over Other Apps",
                "Shows full-screen 'Blocked by Dead Sleep' lockout overlay",
                hasOverlayPermission(context),
                getOverlaySettingsIntent(context)
        ));

        list.add(new PermissionItem(
                "exact_alarms",
                "Exact Alarms",
                "Guarantees wake alarms and nudges fire precisely on time",
                hasExactAlarmPermission(context),
                getExactAlarmSettingsIntent(context)
        ));

        list.add(new PermissionItem(
                "device_admin",
                "Device Administrator",
                "Prevents app uninstall during active sleep mode",
                isDeviceAdminActive(context),
                getDeviceAdminRequestIntent(context)
        ));

        list.add(new PermissionItem(
                "battery",
                "Battery Optimization",
                "Prevents Android from killing alarm services in Doze mode",
                isIgnoringBatteryOptimizations(context),
                getIgnoreBatteryOptimizationIntent(context)
        ));

        list.add(new PermissionItem(
                "fullscreen",
                "Full-Screen Alarm Intent",
                "Displays the ringing alarm screen directly over your lock screen",
                hasFullScreenIntentPermission(context),
                getFullScreenIntentSettings(context)
        ));

        return list;
    }

    public static boolean areAllCriticalPermissionsGranted(Context context) {
        return hasNotificationPermission(context)
                && hasOverlayPermission(context)
                && isAccessibilityServiceEnabled(context)
                && hasExactAlarmPermission(context);
    }
}
