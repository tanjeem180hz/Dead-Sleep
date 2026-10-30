package com.deadsleep.app;

import static org.junit.Assert.*;

import android.view.View;

import com.deadsleep.app.util.AnimationHelper;

import org.junit.Test;

/**
 * Unit tests for AnimationHelper ensuring null-safety and stability across all callers.
 */
public class AnimationHelperTest {

    @Test
    public void testNullViewSafety() {
        // Must handle null safely without throwing NullPointerException
        AnimationHelper.attachBounceFeedback(null);
        assertNull(AnimationHelper.startPulseBreathing(null));
        assertNull(AnimationHelper.startFloatingAnimation(null, 10f));
        AnimationHelper.animateSlideUp(null, 100);
        AnimationHelper.animateStaggeredEntrance((View[]) null);
        AnimationHelper.animateStaggeredEntrance((View) null, (View) null);
        AnimationHelper.pop(null);
    }
}
