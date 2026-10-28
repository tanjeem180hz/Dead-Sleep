package com.deadsleep.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.deadsleep.app.util.TimeUtils;

import org.junit.Test;

public class TimeUtilsTest {

    @Test
    public void testMidnightCrossingDuration() {
        // 23:30 to 06:30 is 7 hours = 420 minutes
        int duration = TimeUtils.calculateDurationMinutes(23, 30, 6, 30);
        assertEquals(420, duration);
    }

    @Test
    public void testSameDayDuration() {
        // 01:00 to 09:30 is 8 hours 30 min = 510 minutes
        int duration = TimeUtils.calculateDurationMinutes(1, 0, 9, 30);
        assertEquals(510, duration);
    }

    @Test
    public void testFormatDuration() {
        assertEquals("7h", TimeUtils.formatDuration(420));
        assertEquals("8h 30m", TimeUtils.formatDuration(510));
    }

    @Test
    public void testFormatDays() {
        assertEquals("Everyday", TimeUtils.formatDays(TimeUtils.ALL_DAYS));
        assertEquals("Weekdays", TimeUtils.formatDays(TimeUtils.WEEKDAYS));
        assertEquals("Weekends", TimeUtils.formatDays(TimeUtils.WEEKENDS));
        assertEquals("Never", TimeUtils.formatDays(0));
    }

    @Test
    public void testCountdownFormatting() {
        // 3661 seconds = 1 hour, 1 minute, 1 second
        assertEquals("01:01:01", TimeUtils.formatCountdown(3661 * 1000L));
        // 65 seconds = 1 minute, 5 seconds
        assertEquals("01:05", TimeUtils.formatCountdown(65 * 1000L));
    }
}
