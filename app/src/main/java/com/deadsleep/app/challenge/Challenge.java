package com.deadsleep.app.challenge;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.deadsleep.app.util.Constants;

/**
 * Base abstract class for all cognitive wake-up challenges.
 * Follows OOP abstraction and polymorphism.
 */
public abstract class Challenge {

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD;

        public static Difficulty fromString(String diff) {
            if (Constants.DIFFICULTY_EASY.equalsIgnoreCase(diff)) return EASY;
            if (Constants.DIFFICULTY_HARD.equalsIgnoreCase(diff)) return HARD;
            return MEDIUM;
        }
    }

    public interface ChallengeCallback {
        void onChallengeSolved();
        void onChallengeFailed(String reason);
        void onProgressUpdated(int current, int total);
    }

    protected final Difficulty difficulty;
    protected int totalProblems = 1;
    protected int currentProgress = 0;

    public Challenge(Difficulty difficulty) {
        this.difficulty = difficulty != null ? difficulty : Difficulty.MEDIUM;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getTotalProblems() {
        return totalProblems;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    /**
     * Generates challenge puzzle parameters.
     */
    public abstract void generate();

    /**
     * Validates a submitted answer.
     */
    public abstract boolean validate(Object answer);

    /**
     * Returns the human-readable display title.
     */
    public abstract String getTitle();

    /**
     * Returns clear instructions for the user.
     */
    public abstract String getInstructions();

    /**
     * Dynamically renders and binds the interactive puzzle view.
     */
    public abstract View renderView(Context context, ViewGroup container, ChallengeCallback callback);

    /**
     * Cleanup resources (listeners, sensors, animations).
     */
    public void cleanup() {}
}
