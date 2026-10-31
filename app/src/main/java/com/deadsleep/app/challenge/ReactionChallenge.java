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

import com.deadsleep.app.R;
import com.deadsleep.app.util.AnimationHelper;

import java.util.Random;

/**
 * Reaction time test measuring cognitive alertness before waking.
 * Configured with friendly human reaction thresholds and forgiving retry rules.
 */
public class ReactionChallenge extends Challenge {

    private enum State {
        WAITING_TO_START,
        RED_WAIT,
        GREEN_TAP,
        SUCCESS,
        FALSE_START
    }

    private final int targetRounds;
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
                this.targetRounds = 1;
                this.thresholdMs = 850;
                break;
            case HARD:
                this.targetRounds = 3;
                this.thresholdMs = 450;
                break;
            case MEDIUM:
            default:
                this.targetRounds = 2; // Friendly: 2 quick rounds (was 3)
                this.thresholdMs = 650; // Comfortable 650ms threshold (was 480ms)
                break;
        }
        this.totalProblems = targetRounds;
        generate();
    }

    @Override
    public String getIconEmoji() {
        return "⚡";
    }

    @Override
    public int getIconDrawableRes() {
        return R.drawable.ic_challenge_reaction;
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
        return "⚡ Reaction Alert Test";
    }

    @Override
    public String getInstructions() {
        return "Wait for the box to turn GREEN, then tap! Target: < " + thresholdMs + "ms.";
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

        // Tactile bounce feedback
        AnimationHelper.attachBounceFeedback(card);

        Runnable triggerGreen = () -> {
            currentState = State.GREEN_TAP;
            greenTimestamp = System.currentTimeMillis();
            card.setCardBackgroundColor(Color.parseColor("#2E7D32")); // Green
            tvPrompt.setText("⚡ TAP NOW! ⚡");
            tvSub.setText("Quickly tap anywhere in the box!");
            AnimationHelper.pop(card);
        };

        card.setOnClickListener(v -> {
            if (currentState == State.WAITING_TO_START || currentState == State.FALSE_START) {
                // Begin waiting round
                currentState = State.RED_WAIT;
                card.setCardBackgroundColor(Color.parseColor("#801E23"));
                tvPrompt.setText("Wait for GREEN...");
                tvSub.setText("Get ready to tap...");

                // Random delay between 1.5 and 3.5 seconds
                long delay = 1500 + random.nextInt(2000);
                handler.postDelayed(triggerGreen, delay);

            } else if (currentState == State.RED_WAIT) {
                // False start! Friendly retry without losing previous completed rounds
                handler.removeCallbacks(triggerGreen);
                currentState = State.FALSE_START;
                card.setCardBackgroundColor(Color.parseColor("#5A1014"));
                tvPrompt.setText("A little too early! 😊");
                tvSub.setText("Tap here to try this round again.");
                callback.onChallengeFailed("Too early! Wait for the box to turn green.");
                AnimationHelper.pop(card);

            } else if (currentState == State.GREEN_TAP) {
                long reactionTime = System.currentTimeMillis() - greenTimestamp;
                if (validate(reactionTime)) {
                    completedRounds++;
                    currentProgress = completedRounds;
                    callback.onProgressUpdated(completedRounds, targetRounds);
                    AnimationHelper.pop(card);

                    if (completedRounds >= targetRounds) {
                        currentState = State.SUCCESS;
                        card.setCardBackgroundColor(Color.parseColor("#1B5E20"));
                        tvPrompt.setText("Passed! (" + reactionTime + "ms) ☀️");
                        tvSub.setText("Great reflexes! Challenge solved.");
                        callback.onChallengeSolved();
                    } else {
                        currentState = State.WAITING_TO_START;
                        tvHeader.setText("Round " + (completedRounds + 1) + " of " + targetRounds);
                        card.setCardBackgroundColor(Color.parseColor("#35248A"));
                        tvPrompt.setText("Great! (" + reactionTime + "ms)");
                        tvSub.setText("Tap to start Round " + (completedRounds + 1));
                    }
                } else {
                    // Missed threshold
                    currentState = State.WAITING_TO_START;
                    card.setCardBackgroundColor(Color.parseColor("#801E23"));
                    tvPrompt.setText(reactionTime + "ms (Target < " + thresholdMs + "ms)");
                    tvSub.setText("Almost there! Tap to try again.");
                    callback.onChallengeFailed("A little slow (" + reactionTime + "ms). Try again!");
                    AnimationHelper.pop(card);
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
