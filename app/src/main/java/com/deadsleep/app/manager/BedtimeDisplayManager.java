package com.deadsleep.app.manager;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Build;
import android.provider.Settings;
import android.view.View;

/**
 * Manages Bedtime Mode display desaturation (system-wide Monochromacy/Grayscale and in-app color filtering).
 */
public final class BedtimeDisplayManager {

    private static final String DALTONIZER_ENABLED_KEY = "accessibility_display_daltonizer_enabled";
    private static final String DALTONIZER_MODE_KEY = "accessibility_display_daltonizer";
    // 0 = Monochromacy (pure black and white grayscale)
    private static final int MONOCHROMACY_MODE = 0;

    private BedtimeDisplayManager() {}

    /**
     * Checks if the app has been granted android.permission.WRITE_SECURE_SETTINGS.
     */
    public static boolean hasWriteSecurePermission(Context context) {
        if (context == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * Checks if system-wide grayscale display is currently active.
     */
    public static boolean isGrayscaleActive(Context context) {
        if (context == null) return false;
        try {
            ContentResolver cr = context.getContentResolver();
            int enabled = Settings.Secure.getInt(cr, DALTONIZER_ENABLED_KEY, 0);
            int mode = Settings.Secure.getInt(cr, DALTONIZER_MODE_KEY, -1);
            return enabled == 1 && mode == MONOCHROMACY_MODE;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Turns the phone's display into Black & White (Grayscale) using Android's Monochromacy daltonizer.
     * Returns true if system-wide grayscale was successfully toggled, false if permission is needed.
     */
    public static boolean enableGrayscale(Context context) {
        if (context == null) return false;
        try {
            ContentResolver cr = context.getContentResolver();
            Settings.Secure.putInt(cr, DALTONIZER_MODE_KEY, MONOCHROMACY_MODE);
            Settings.Secure.putInt(cr, DALTONIZER_ENABLED_KEY, 1);
            return true;
        } catch (SecurityException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Disables Grayscale and restores the phone's display to full, vibrant color.
     */
    public static boolean disableGrayscale(Context context) {
        if (context == null) return false;
        try {
            ContentResolver cr = context.getContentResolver();
            Settings.Secure.putInt(cr, DALTONIZER_ENABLED_KEY, 0);
            return true;
        } catch (SecurityException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Safely opens Android's Color Correction / Grayscale Settings screen.
     */
    public static void openColorCorrectionSettings(Activity activity) {
        if (activity == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent intent = new Intent(Settings.ACTION_COLOR_CORRECTION_SETTINGS);
                activity.startActivity(intent);
                return;
            }
        } catch (Exception ignored) {}

        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            activity.startActivity(intent);
        } catch (Exception ignored) {
            try {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                activity.startActivity(intent);
            } catch (Exception ignored2) {}
        }
    }

    /**
     * Renders any view and its children in 100% black and white grayscale using a hardware ColorMatrix filter.
     */
    public static void applyGrayscaleToView(View view) {
        if (view == null) return;
        try {
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0f); // 0 = complete black and white desaturation
            Paint paint = new Paint();
            paint.setColorFilter(new ColorMatrixColorFilter(cm));
            view.setLayerType(View.LAYER_TYPE_HARDWARE, paint);
        } catch (Exception ignored) {}
    }

    /**
     * Restores normal full color rendering for a view.
     */
    public static void removeGrayscaleFromView(View view) {
        if (view == null) return;
        try {
            view.setLayerType(View.LAYER_TYPE_NONE, null);
        } catch (Exception ignored) {}
    }
}
