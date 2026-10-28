package com.deadsleep.app.util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

/**
 * Manages vibration patterns for the wake-up alarm and touch feedback.
 */
public class VibrationHelper {

    private final Vibrator vibrator;
    private boolean isVibrating = false;

    // Standard alarm vibration pattern: [delay, vibrate, pause, vibrate, pause, ...]
    private static final long[] ALARM_PATTERN = {0, 600, 300, 600, 300, 1000};
    private static final int[] ALARM_AMPLITUDES = {0, 255, 0, 255, 0, 255};

    public VibrationHelper(Context context) {
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Starts looping alarm vibration.
     */
    public void startAlarmVibration() {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        isVibrating = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                VibrationEffect effect = VibrationEffect.createWaveform(ALARM_PATTERN, ALARM_AMPLITUDES, 0);
                vibrator.vibrate(effect);
            } catch (Exception e) {
                VibrationEffect effect = VibrationEffect.createWaveform(ALARM_PATTERN, 0);
                vibrator.vibrate(effect);
            }
        } else {
            vibrator.vibrate(ALARM_PATTERN, 0);
        }
    }

    /**
     * Produces a short haptic feedback click.
     */
    public void vibrateTick() {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(40);
        }
    }

    /**
     * Produces a success double-buzz pattern.
     */
    public void vibrateSuccess() {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        long[] pattern = {0, 80, 80, 120};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        } else {
            vibrator.vibrate(pattern, -1);
        }
    }

    public void stop() {
        if (vibrator != null) {
            vibrator.cancel();
        }
        isVibrating = false;
    }

    public boolean isVibrating() {
        return isVibrating;
    }
}
