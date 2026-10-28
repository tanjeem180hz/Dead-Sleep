package com.deadsleep.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.deadsleep.app.model.RoutineSuggestion;

import org.junit.Test;

public class RoutineAdvisorTest {

    @Test
    public void testRoutineSuggestionCreation() {
        RoutineSuggestion suggestion = new RoutineSuggestion(
                "test_snooze",
                "High Snoozes",
                "You snoozed often",
                "SNOOZE_OVERUSE",
                "Apply"
        );
        assertEquals("test_snooze", suggestion.getId());
        assertEquals("SNOOZE_OVERUSE", suggestion.getRuleType());
        assertFalse(suggestion.isApplied());

        suggestion.setApplied(true);
        assertTrue(suggestion.isApplied());
    }

    private static void assertFalse(boolean applied) {
        org.junit.Assert.assertFalse(applied);
    }
}
