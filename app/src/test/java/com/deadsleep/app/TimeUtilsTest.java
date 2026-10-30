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

    @Test
    public void testFormatCountdownVerbose() {
        assertEquals("01h 01m 01s", TimeUtils.formatCountdownVerbose(3661 * 1000L));
        assertEquals("01m 05s", TimeUtils.formatCountdownVerbose(65 * 1000L));
        assertEquals("15s", TimeUtils.formatCountdownVerbose(15 * 1000L));
        assertEquals("00m 00s", TimeUtils.formatCountdownVerbose(0));
    }

    @Test
    public void testGetTargetWakeTimeMillis() {
        java.util.Calendar sleepCal = java.util.Calendar.getInstance();
        sleepCal.set(2026, java.util.Calendar.OCTOBER, 30, 23, 0, 0); // Oct 30, 23:00
        sleepCal.set(java.util.Calendar.MILLISECOND, 0);

        long wakeMillis = TimeUtils.getTargetWakeTimeMillis(sleepCal.getTimeInMillis(), 7, 0);
        java.util.Calendar wakeCal = java.util.Calendar.getInstance();
        wakeCal.setTimeInMillis(wakeMillis);

        assertEquals(2026, wakeCal.get(java.util.Calendar.YEAR));
        assertEquals(java.util.Calendar.OCTOBER, wakeCal.get(java.util.Calendar.MONTH));
        assertEquals(31, wakeCal.get(java.util.Calendar.DAY_OF_MONTH)); // Next morning: Oct 31
        assertEquals(7, wakeCal.get(java.util.Calendar.HOUR_OF_DAY));
        assertEquals(0, wakeCal.get(java.util.Calendar.MINUTE));
    }

    @Test
    public void testFormatTime() {
        assertEquals("11:00 PM", TimeUtils.formatTime(23, 0));
        assertEquals("7:00 AM", TimeUtils.formatTime(7, 0));
        assertEquals("23:00", TimeUtils.formatTime24(23, 0));
        assertEquals("07:00", TimeUtils.formatTime24(7, 0));
    }
}
