package com.deadsleep.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.deadsleep.app.model.SleepSession;

import org.junit.Test;

public class StatsLogicTest {

    @Test
    public void testSleepSessionDurationCalculation() {
        SleepSession session = new SleepSession();
        long start = System.currentTimeMillis();
        long end = start + (7 * 3600 * 1000L); // 7 hours later

        session.setStartTimeMillis(start);
        session.setActualWakeTimeMillis(end);

        assertEquals(420, session.getActualDurationMinutes());
    }

    @Test
    public void testConsistencyScoreBounds() {
        int base = 100;
        int snoozes = 3;
        int blocked = 4;
        int penalty = (snoozes * 4) + (blocked * 2);
        int score = Math.max(20, Math.min(100, base - penalty));

        assertTrue(score >= 20 && score <= 100);
        assertEquals(80, score);
    }
}
