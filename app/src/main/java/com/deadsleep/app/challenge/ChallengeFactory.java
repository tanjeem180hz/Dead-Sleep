package com.deadsleep.app.challenge;

import android.content.Context;

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

    public static Challenge createChallenge(Context context, String type, String difficultyStr) {
        Challenge.Difficulty difficulty = Challenge.Difficulty.fromString(difficultyStr);

        if (Constants.CHALLENGE_RANDOM.equalsIgnoreCase(type) || type == null || type.isEmpty()) {
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
}
