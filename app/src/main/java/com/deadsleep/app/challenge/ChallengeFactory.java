package com.deadsleep.app.challenge;

import android.content.Context;

import com.deadsleep.app.R;
import com.deadsleep.app.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Factory for creating cognitive Challenge instances based on type and difficulty.
 */
public final class ChallengeFactory {

    private static final Random RANDOM = new Random();

    private ChallengeFactory() {}

    public static String normalizeChallengeType(String input) {
        if (input == null || input.trim().isEmpty()) return Constants.CHALLENGE_RANDOM;
        String upper = input.toUpperCase();
        if (upper.contains("MATH")) return Constants.CHALLENGE_MATH;
        if (upper.contains("MEMORY")) return Constants.CHALLENGE_MEMORY;
        if (upper.contains("PATTERN")) return Constants.CHALLENGE_PATTERN;
        if (upper.contains("REACTION")) return Constants.CHALLENGE_REACTION;
        if (upper.contains("SHAKE")) return Constants.CHALLENGE_SHAKE;
        return Constants.CHALLENGE_RANDOM;
    }

    public static Challenge createChallenge(Context context, String rawType, String difficultyStr) {
        Challenge.Difficulty difficulty = Challenge.Difficulty.fromString(difficultyStr);
        String type = normalizeChallengeType(rawType);

        if (Constants.CHALLENGE_RANDOM.equalsIgnoreCase(type)) {
            List<String> pool = new ArrayList<>();
            pool.add(Constants.CHALLENGE_MATH);
            pool.add(Constants.CHALLENGE_MEMORY);
            pool.add(Constants.CHALLENGE_PATTERN);
            pool.add(Constants.CHALLENGE_REACTION);

            if (ShakeChallenge.isSensorAvailable(context)) {
                pool.add(Constants.CHALLENGE_SHAKE);
            }
            type = pool.get(RANDOM.nextInt(pool.size()));
        }

        switch (type.toUpperCase()) {
            case Constants.CHALLENGE_MATH:
                return new MathChallenge(difficulty);

            case Constants.CHALLENGE_MEMORY:
                return new MemoryChallenge(difficulty);

            case Constants.CHALLENGE_PATTERN:
                return new PatternMemoryChallenge(difficulty);

            case Constants.CHALLENGE_REACTION:
                return new ReactionChallenge(difficulty);

            case Constants.CHALLENGE_SHAKE:
                if (ShakeChallenge.isSensorAvailable(context)) {
                    return new ShakeChallenge(difficulty);
                } else {
                    return new MathChallenge(difficulty);
                }

            default:
                return new MathChallenge(difficulty);
        }
    }

    public static String[] getAvailableChallengeTypes(Context context) {
        if (ShakeChallenge.isSensorAvailable(context)) {
            return new String[]{
                    Constants.CHALLENGE_RANDOM,
                    Constants.CHALLENGE_MATH,
                    Constants.CHALLENGE_MEMORY,
                    Constants.CHALLENGE_PATTERN,
                    Constants.CHALLENGE_REACTION,
                    Constants.CHALLENGE_SHAKE
            };
        } else {
            return new String[]{
                    Constants.CHALLENGE_RANDOM,
                    Constants.CHALLENGE_MATH,
                    Constants.CHALLENGE_MEMORY,
                    Constants.CHALLENGE_PATTERN,
                    Constants.CHALLENGE_REACTION
            };
        }
    }

    public static String getChallengeIconEmoji(String type) {
        if (type == null) return "🎲";
        switch (type.toUpperCase()) {
            case Constants.CHALLENGE_MATH: return "🔢";
            case Constants.CHALLENGE_MEMORY: return "🃏";
            case Constants.CHALLENGE_PATTERN: return "🧩";
            case Constants.CHALLENGE_REACTION: return "⚡";
            case Constants.CHALLENGE_SHAKE: return "📳";
            case Constants.CHALLENGE_RANDOM:
            default:
                return "🎲";
        }
    }

    public static int getChallengeIconDrawableRes(String type) {
        if (type == null) return R.drawable.ic_challenge_random;
        switch (type.toUpperCase()) {
            case Constants.CHALLENGE_MATH: return R.drawable.ic_challenge_math;
            case Constants.CHALLENGE_MEMORY: return R.drawable.ic_challenge_memory;
            case Constants.CHALLENGE_PATTERN: return R.drawable.ic_challenge_pattern;
            case Constants.CHALLENGE_REACTION: return R.drawable.ic_challenge_reaction;
            case Constants.CHALLENGE_SHAKE: return R.drawable.ic_challenge_shake;
            case Constants.CHALLENGE_RANDOM:
            default:
                return R.drawable.ic_challenge_random;
        }
    }

    public static String getChallengeDisplayName(String type) {
        if (type == null) return "🎲 Surprise Challenge (Mix)";
        switch (type.toUpperCase()) {
            case Constants.CHALLENGE_MATH: return "🔢 Math Puzzle";
            case Constants.CHALLENGE_MEMORY: return "🃏 Memory Match";
            case Constants.CHALLENGE_PATTERN: return "🧩 Pattern Sequence";
            case Constants.CHALLENGE_REACTION: return "⚡ Reaction Test";
            case Constants.CHALLENGE_SHAKE: return "📳 Shake-to-Wake";
            case Constants.CHALLENGE_RANDOM:
            default:
                return "🎲 Surprise Challenge (Mix)";
        }
    }

    public static String getChallengeFriendlyDescription(String type) {
        if (type == null) return "Random cognitive challenge every morning";
        switch (type.toUpperCase()) {
            case Constants.CHALLENGE_MATH: return "Solve 2 quick, friendly math questions";
            case Constants.CHALLENGE_MEMORY: return "Find 3 matching pairs of soothing symbols";
            case Constants.CHALLENGE_PATTERN: return "Repeat a simple 4-step glowing color pattern";
            case Constants.CHALLENGE_REACTION: return "Tap the screen as soon as it turns green";
            case Constants.CHALLENGE_SHAKE: return "Gently shake your phone to dismiss";
            case Constants.CHALLENGE_RANDOM:
            default:
                return "A fun, friendly random wake challenge each morning";
        }
    }
}
