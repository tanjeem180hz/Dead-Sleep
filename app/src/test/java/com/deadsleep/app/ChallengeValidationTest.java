package com.deadsleep.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.deadsleep.app.challenge.Challenge;
import com.deadsleep.app.challenge.MathChallenge;
import com.deadsleep.app.challenge.MemoryChallenge;
import com.deadsleep.app.challenge.PatternMemoryChallenge;
import com.deadsleep.app.challenge.ReactionChallenge;
import com.deadsleep.app.challenge.ShakeChallenge;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ChallengeValidationTest {

    @Test
    public void testMathChallengeGeneration() {
        MathChallenge easy = new MathChallenge(Challenge.Difficulty.EASY);
        assertEquals(1, easy.getTotalProblems());

        MathChallenge medium = new MathChallenge(Challenge.Difficulty.MEDIUM);
        assertEquals(3, medium.getTotalProblems());

        MathChallenge hard = new MathChallenge(Challenge.Difficulty.HARD);
        assertEquals(5, hard.getTotalProblems());
    }

    @Test
    public void testMemoryChallengeTotalPairs() {
        MemoryChallenge easy = new MemoryChallenge(Challenge.Difficulty.EASY);
        assertEquals(6, easy.getTotalProblems());

        MemoryChallenge medium = new MemoryChallenge(Challenge.Difficulty.MEDIUM);
        assertEquals(8, medium.getTotalProblems());

        MemoryChallenge hard = new MemoryChallenge(Challenge.Difficulty.HARD);
        assertEquals(10, hard.getTotalProblems());
    }

    @Test
    public void testReactionChallengeValidation() {
        ReactionChallenge reaction = new ReactionChallenge(Challenge.Difficulty.MEDIUM);
        // Under threshold (480ms) should be valid
        assertTrue(reaction.validate(250L));
        // Above threshold should be invalid
        assertFalse(reaction.validate(520L));
        // Negative / zero should be invalid
        assertFalse(reaction.validate(-10L));
    }

    @Test
    public void testShakeChallengeTotalShakes() {
        ShakeChallenge easy = new ShakeChallenge(Challenge.Difficulty.EASY);
        assertEquals(20, easy.getTotalProblems());

        ShakeChallenge medium = new ShakeChallenge(Challenge.Difficulty.MEDIUM);
        assertEquals(40, medium.getTotalProblems());

        ShakeChallenge hard = new ShakeChallenge(Challenge.Difficulty.HARD);
        assertEquals(60, hard.getTotalProblems());
    }
}
