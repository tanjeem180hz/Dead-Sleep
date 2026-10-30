package com.deadsleep.app;

import static org.junit.Assert.*;

import com.deadsleep.app.manager.BedtimeDisplayManager;

import org.junit.Test;

/**
 * Unit tests for BedtimeDisplayManager and safety boundaries.
 */
public class BedtimeDisplayManagerTest {

    @Test
    public void testNullContextSafety() {
        assertFalse(BedtimeDisplayManager.hasWriteSecurePermission(null));
        assertFalse(BedtimeDisplayManager.isGrayscaleActive(null));
        assertFalse(BedtimeDisplayManager.enableGrayscale(null));
        assertFalse(BedtimeDisplayManager.disableGrayscale(null));
    }

    @Test
    public void testNullViewSafety() {
        // Must execute cleanly without NullPointerException or crash
        BedtimeDisplayManager.applyGrayscaleToView(null);
        BedtimeDisplayManager.removeGrayscaleFromView(null);
        BedtimeDisplayManager.openColorCorrectionSettings(null);
    }
}
