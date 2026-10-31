package com.deadsleep.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.deadsleep.app.challenge.Challenge;
import com.deadsleep.app.challenge.ChallengeFactory;
import com.deadsleep.app.challenge.MathChallenge;
import com.deadsleep.app.challenge.MemoryChallenge;
import com.deadsleep.app.challenge.PatternMemoryChallenge;
import com.deadsleep.app.challenge.ReactionChallenge;
import com.deadsleep.app.challenge.ShakeChallenge;
import com.deadsleep.app.util.Constants;

import org.junit.Test;

public class ChallengeValidationTest {

    @Test
    public void testMathChallengeGenerationAndEasing() {
        MathChallenge easy = new MathChallenge(Challenge.Difficulty.EASY);
        assertEquals(1, easy.getTotalProblems());
        assertEquals("🔢", easy.getIconEmoji());

        // Medium is eased down from 3 hard problems to 2 quick friendly problems
        MathChallenge medium = new MathChallenge(Challenge.Difficulty.MEDIUM);
        assertEquals(2, medium.getTotalProblems());
        assertEquals("🔢", medium.getIconEmoji());

        MathChallenge hard = new MathChallenge(Challenge.Difficulty.HARD);
        assertEquals(4, hard.getTotalProblems());
    }

    @Test
    public void testMemoryChallengeTotalPairsAndEasing() {
        // EASY reduced from 6 pairs to 2 pairs (4 cards total)
        MemoryChallenge easy = new MemoryChallenge(Challenge.Difficulty.EASY);
        assertEquals(2, easy.getTotalProblems());
        assertEquals("🃏", easy.getIconEmoji());

        // MEDIUM reduced from 8 pairs (16 cards) to 3 pairs (6 cards total) for waking up easily
        MemoryChallenge medium = new MemoryChallenge(Challenge.Difficulty.MEDIUM);
        assertEquals(3, medium.getTotalProblems());
        assertEquals("🃏", medium.getIconEmoji());

        // HARD reduced from 10 pairs to 4 pairs (8 cards total)
        MemoryChallenge hard = new MemoryChallenge(Challenge.Difficulty.HARD);
        assertEquals(4, hard.getTotalProblems());
    }

    @Test
    public void testPatternMemoryChallengeStepsAndEasing() {
        PatternMemoryChallenge easy = new PatternMemoryChallenge(Challenge.Difficulty.EASY);
        assertEquals(3, easy.getTotalProblems());
        assertEquals("🧩", easy.getIconEmoji());

        // MEDIUM reduced from 6 steps to 4 steps
        PatternMemoryChallenge medium = new PatternMemoryChallenge(Challenge.Difficulty.MEDIUM);
        assertEquals(4, medium.getTotalProblems());
        assertEquals("🧩", medium.getIconEmoji());

        PatternMemoryChallenge hard = new PatternMemoryChallenge(Challenge.Difficulty.HARD);
        assertEquals(6, hard.getTotalProblems());
    }

    @Test
    public void testReactionChallengeValidationAndEasing() {
        ReactionChallenge reaction = new ReactionChallenge(Challenge.Difficulty.MEDIUM);
        assertEquals("⚡", reaction.getIconEmoji());
        assertEquals(2, reaction.getTotalProblems());

        // Under relaxed threshold (650ms for Medium) should be valid
        assertTrue(reaction.validate(250L));
        assertTrue(reaction.validate(520L));
        assertTrue(reaction.validate(640L));

        // Above threshold should be invalid
        assertFalse(reaction.validate(750L));

        // Negative / zero should be invalid
        assertFalse(reaction.validate(-10L));
    }

    @Test
    public void testShakeChallengeTotalShakesAndEasing() {
        // EASY reduced from 20 to 10 gentle shakes
        ShakeChallenge easy = new ShakeChallenge(Challenge.Difficulty.EASY);
        assertEquals(10, easy.getTotalProblems());
        assertEquals("📳", easy.getIconEmoji());

        // MEDIUM reduced from 40 to 18 gentle shakes (~5-6 sec)
        ShakeChallenge medium = new ShakeChallenge(Challenge.Difficulty.MEDIUM);
        assertEquals(18, medium.getTotalProblems());
        assertEquals("📳", medium.getIconEmoji());

        // HARD reduced from 60 to 32 shakes
        ShakeChallenge hard = new ShakeChallenge(Challenge.Difficulty.HARD);
        assertEquals(32, hard.getTotalProblems());
    }

    @Test
    public void testChallengeFactoryHelpersAndIcons() {
        assertEquals("🔢", ChallengeFactory.getChallengeIconEmoji(Constants.CHALLENGE_MATH));
        assertEquals("🃏", ChallengeFactory.getChallengeIconEmoji(Constants.CHALLENGE_MEMORY));
        assertEquals("🧩", ChallengeFactory.getChallengeIconEmoji(Constants.CHALLENGE_PATTERN));
        assertEquals("⚡", ChallengeFactory.getChallengeIconEmoji(Constants.CHALLENGE_REACTION));
        assertEquals("📳", ChallengeFactory.getChallengeIconEmoji(Constants.CHALLENGE_SHAKE));
        assertEquals("🎲", ChallengeFactory.getChallengeIconEmoji(Constants.CHALLENGE_RANDOM));

        // Normalization handles mixed strings and emojis gracefully
        assertEquals(Constants.CHALLENGE_MATH, ChallengeFactory.normalizeChallengeType("🔢 Math Puzzle"));
        assertEquals(Constants.CHALLENGE_MEMORY, ChallengeFactory.normalizeChallengeType("🃏 Memory Match"));
        assertEquals(Constants.CHALLENGE_PATTERN, ChallengeFactory.normalizeChallengeType("🧩 Pattern Sequence"));
        assertEquals(Constants.CHALLENGE_REACTION, ChallengeFactory.normalizeChallengeType("⚡ Reaction Test"));
        assertEquals(Constants.CHALLENGE_SHAKE, ChallengeFactory.normalizeChallengeType("📳 Shake-to-Wake"));
        assertEquals(Constants.CHALLENGE_RANDOM, ChallengeFactory.normalizeChallengeType(null));

        assertNotNull(ChallengeFactory.getChallengeDisplayName(Constants.CHALLENGE_MATH));
        assertNotNull(ChallengeFactory.getChallengeFriendlyDescription(Constants.CHALLENGE_MATH));
    }
}
