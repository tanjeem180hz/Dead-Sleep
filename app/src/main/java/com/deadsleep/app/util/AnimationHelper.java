package com.deadsleep.app.util;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Utility helper providing fluid tactile feedback, gentle breathing/pulsing animations,
 * spring bounces, and staggered card entrance transitions.
 */
public final class AnimationHelper {

    private AnimationHelper() {}

    /**
     * Attaches tactile spring bounce feedback to any interactive View.
     * When touched down, the view smoothly contracts to 95% scale; on release,
     * it bounces back with an energetic overshoot spring and performs a subtle haptic tick.
     */
    @SuppressLint("ClickableViewAccessibility")
    public static void attachBounceFeedback(View view) {
        if (view == null) return;
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(120)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                    try {
                        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    } catch (Exception ignored) {}
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(220)
                            .setInterpolator(new OvershootInterpolator(2.2f))
                            .start();
                    break;
            }
            return false; // return false to allow onClick to execute
        });
    }

    /**
     * Starts an infinite gentle breathing/pulse animation (e.g. for moon logo or sleep countdown).
     * Mimics a deep, soothing 3.6-second sleep respiration cycle.
     */
    public static AnimatorSet startPulseBreathing(View view) {
        if (view == null) return null;

        PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.06f);
        PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.06f);
        PropertyValuesHolder pvhAlpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.90f, 1.0f);

        ObjectAnimator pulseAnim = ObjectAnimator.ofPropertyValuesHolder(view, pvhScaleX, pvhScaleY, pvhAlpha);
        pulseAnim.setDuration(1800);
        pulseAnim.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnim.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet set = new AnimatorSet();
        set.play(pulseAnim);
        set.start();
        return set;
    }

    /**
     * Starts a subtle vertical floating animation (e.g. for badges, illustration moons).
     */
    public static ObjectAnimator startFloatingAnimation(View view, float distanceDp) {
        if (view == null) return null;
        float distancePx = distanceDp * view.getResources().getDisplayMetrics().density;

        ObjectAnimator floatAnim = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, -distancePx, distancePx);
        floatAnim.setDuration(2400);
        floatAnim.setRepeatMode(ValueAnimator.REVERSE);
        floatAnim.setRepeatCount(ValueAnimator.INFINITE);
        floatAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        floatAnim.start();
        return floatAnim;
    }

    /**
     * Smooth slide-up fade-in entrance for a single view.
     */
    public static void animateSlideUp(View view, long delayMillis) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setTranslationY(60f);

        view.animate()
                .alpha(1.0f)
                .translationY(0f)
                .setStartDelay(delayMillis)
                .setDuration(380)
                .setInterpolator(new DecelerateInterpolator(1.8f))
                .start();
    }

    /**
     * Cascades a staggered entrance animation across an array of views.
     */
    public static void animateStaggeredEntrance(View... views) {
        if (views == null) return;
        long delay = 50;
        for (View v : views) {
            if (v != null) {
                animateSlideUp(v, delay);
                delay += 75;
            }
        }
    }

    /**
     * Subtle pulse scale burst on action completion or toggle click.
     */
    public static void pop(View view) {
        if (view == null) return;
        view.setScaleX(0.85f);
        view.setScaleY(0.85f);
        view.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(280)
                .setInterpolator(new OvershootInterpolator(3.0f))
                .start();
        try {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        } catch (Exception ignored) {}
    }
}
