package com.deadsleep.app.challenge;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.util.Random;

/**
 * Reaction time test measuring cognitive alertness before waking.
 */
public class ReactionChallenge extends Challenge {

    private enum State {
        WAITING_TO_START,
        RED_WAIT,
        GREEN_TAP,
        SUCCESS,
        FALSE_START
    }

    private final int targetRounds = 3;
    private final int thresholdMs;
    private int completedRounds = 0;
    private long greenTimestamp = 0;
    private State currentState = State.WAITING_TO_START;

    private Handler handler;
    private final Random random = new Random();

    public ReactionChallenge(Difficulty difficulty) {
        super(difficulty);
        switch (difficulty) {
            case EASY:
                this.thresholdMs = 650;
                break;
            case HARD:
                this.thresholdMs = 340;
                break;
            case MEDIUM:
            default:
                this.thresholdMs = 480;
                break;
        }
        this.totalProblems = targetRounds;
        generate();
    }

    @Override
    public void generate() {
        completedRounds = 0;
        currentProgress = 0;
        currentState = State.WAITING_TO_START;
    }

    @Override
    public boolean validate(Object answer) {
        if (!(answer instanceof Long)) return false;
        long time = (Long) answer;
        return time > 0 && time <= thresholdMs;
    }

    @Override
    public String getTitle() {
        return "Reaction Time Test";
    }

    @Override
    public String getInstructions() {
        return "Wait for the box to turn GREEN, then tap as fast as you can! Target: under " + thresholdMs + "ms.";
    }

    @Override
    public View renderView(Context context, ViewGroup container, ChallengeCallback callback) {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(24, 16, 24, 16);

        TextView tvHeader = new TextView(context);
        tvHeader.setText("Round 1 of " + targetRounds + " (Target < " + thresholdMs + "ms)");
        tvHeader.setTextColor(Color.parseColor("#9D85FF"));
        tvHeader.setTextSize(14f);
        tvHeader.setTypeface(Typeface.DEFAULT_BOLD);
        tvHeader.setGravity(Gravity.CENTER);
        tvHeader.setPadding(0, 0, 0, 16);
        root.addView(tvHeader);

        CardView card = new CardView(context);
        card.setCardBackgroundColor(Color.parseColor("#801E23")); // Initial red
        card.setRadius(24f);
        card.setCardElevation(8f);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 500
        );
        cardParams.setMargins(16, 16, 16, 16);
        card.setLayoutParams(cardParams);

        LinearLayout cardContent = new LinearLayout(context);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setGravity(Gravity.CENTER);

        TextView tvPrompt = new TextView(context);
        tvPrompt.setText("Tap here to start");
        tvPrompt.setTextColor(Color.WHITE);
        tvPrompt.setTextSize(26f);
        tvPrompt.setTypeface(Typeface.DEFAULT_BOLD);
        tvPrompt.setGravity(Gravity.CENTER);
        cardContent.addView(tvPrompt);

        TextView tvSub = new TextView(context);
        tvSub.setText("Do not tap until it turns green!");
        tvSub.setTextColor(Color.parseColor("#E5DEFF"));
        tvSub.setTextSize(14f);
        tvSub.setGravity(Gravity.CENTER);
        tvSub.setPadding(0, 8, 0, 0);
        cardContent.addView(tvSub);

        card.addView(cardContent);
        root.addView(card);

        Runnable triggerGreen = () -> {
            currentState = State.GREEN_TAP;
            greenTimestamp = System.currentTimeMillis();
            card.setCardBackgroundColor(Color.parseColor("#2E7D32")); // Green
            tvPrompt.setText("⚡ TAP NOW! ⚡");
            tvSub.setText("Quickly!");
        };

        card.setOnClickListener(v -> {
            if (currentState == State.WAITING_TO_START || currentState == State.FALSE_START) {
                // Begin waiting round
                currentState = State.RED_WAIT;
                card.setCardBackgroundColor(Color.parseColor("#801E23"));
                tvPrompt.setText("Wait for GREEN...");
                tvSub.setText("Get ready...");

                // Random delay between 2 and 4.5 seconds
                long delay = 2000 + random.nextInt(2500);
                handler.postDelayed(triggerGreen, delay);

            } else if (currentState == State.RED_WAIT) {
                // False start!
                handler.removeCallbacks(triggerGreen);
                currentState = State.FALSE_START;
                card.setCardBackgroundColor(Color.parseColor("#5A1014"));
                tvPrompt.setText("Too early! ❌");
                tvSub.setText("Tap to retry this round.");
                callback.onChallengeFailed("Too early! Wait for green.");

            } else if (currentState == State.GREEN_TAP) {
                long reactionTime = System.currentTimeMillis() - greenTimestamp;
                if (validate(reactionTime)) {
                    completedRounds++;
                    currentProgress = completedRounds;
                    callback.onProgressUpdated(completedRounds, targetRounds);

                    if (completedRounds >= targetRounds) {
                        currentState = State.SUCCESS;
                        card.setCardBackgroundColor(Color.parseColor("#1B5E20"));
                        tvPrompt.setText("Passed! (" + reactionTime + "ms) ☀️");
                        tvSub.setText("Challenge solved!");
                        callback.onChallengeSolved();
                    } else {
                        currentState = State.WAITING_TO_START;
                        tvHeader.setText("Round " + (completedRounds + 1) + " of " + targetRounds);
                        card.setCardBackgroundColor(Color.parseColor("#35248A"));
                        tvPrompt.setText("Good! (" + reactionTime + "ms)");
                        tvSub.setText("Tap to start next round.");
                    }
                } else {
                    currentState = State.WAITING_TO_START;
                    callback.onChallengeFailed("Too slow (" + reactionTime + "ms). Must be under " + thresholdMs + "ms.");
                    card.setCardBackgroundColor(Color.parseColor("#801E23"));
                    tvPrompt.setText("Too slow: " + reactionTime + "ms");
                    tvSub.setText("Tap to try again.");
                }
            }
        });

        return root;
    }

    @Override
    public void cleanup() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
