package com.deadsleep.app.challenge;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deadsleep.app.R;
import com.deadsleep.app.util.AnimationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simon-style pattern sequence memory challenge.
 * Scaled to be friendly, rhythmic, and comfortable for morning alertness.
 */
public class PatternMemoryChallenge extends Challenge {

    private static final int[] BASE_COLORS = {
            Color.parseColor("#801E23"), // Dim Red
            Color.parseColor("#17376D"), // Dim Blue
            Color.parseColor("#154F30"), // Dim Green
            Color.parseColor("#7D6608")  // Dim Yellow
    };

    private static final int[] LIT_COLORS = {
            Color.parseColor("#FF5252"), // Bright Red
            Color.parseColor("#448AFF"), // Bright Blue
            Color.parseColor("#69F0AE"), // Bright Green
            Color.parseColor("#FFD740")  // Bright Yellow
    };

    private static final String[] COLOR_NAMES = {"🔴 RED", "🔵 BLUE", "🟢 GREEN", "🟡 YELLOW"};

    private final List<Integer> sequence = new ArrayList<>();
    private final List<Integer> userSequence = new ArrayList<>();
    private final int targetLength;
    private final Random random = new Random();
    private Handler handler;
    private boolean isPlayingSequence = false;

    public PatternMemoryChallenge(Difficulty difficulty) {
        super(difficulty);
        switch (difficulty) {
            case EASY:
                this.targetLength = 3; // Friendly 3 steps for Easy
                break;
            case HARD:
                this.targetLength = 6;
                break;
            case MEDIUM:
            default:
                this.targetLength = 4; // Friendly 4 steps for Medium (was 6)
                break;
        }
        this.totalProblems = targetLength;
        generate();
    }

    @Override
    public String getIconEmoji() {
        return "🧩";
    }

    @Override
    public int getIconDrawableRes() {
        return R.drawable.ic_challenge_pattern;
    }

    @Override
    public void generate() {
        sequence.clear();
        userSequence.clear();
        currentProgress = 0;
        for (int i = 0; i < targetLength; i++) {
            sequence.add(random.nextInt(4));
        }
    }

    @Override
    public boolean validate(Object answer) {
        if (!(answer instanceof List)) return false;
        List<?> list = (List<?>) answer;
        if (list.size() != sequence.size()) return false;
        for (int i = 0; i < sequence.size(); i++) {
            if (!sequence.get(i).equals(list.get(i))) return false;
        }
        return true;
    }

    @Override
    public String getTitle() {
        return "🧩 Pattern Sequence Challenge";
    }

    @Override
    public String getInstructions() {
        return "Watch the " + targetLength + "-step glowing color pattern, then repeat it.";
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

        TextView tvStatus = new TextView(context);
        tvStatus.setText("Watch the pattern...");
        tvStatus.setTextColor(Color.parseColor("#FFD56B"));
        tvStatus.setTextSize(18f);
        tvStatus.setTypeface(Typeface.DEFAULT_BOLD);
        tvStatus.setGravity(Gravity.CENTER);
        tvStatus.setPadding(0, 0, 0, 16);
        root.addView(tvStatus);

        TextView tvStep = new TextView(context);
        tvStep.setText("Length: " + targetLength + " steps");
        tvStep.setTextColor(Color.parseColor("#9D85FF"));
        tvStep.setTextSize(14f);
        tvStep.setGravity(Gravity.CENTER);
        tvStep.setPadding(0, 0, 0, 24);
        root.addView(tvStep);

        GridLayout grid = new GridLayout(context);
        grid.setColumnCount(2);
        grid.setRowCount(2);
        grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        glp.gravity = Gravity.CENTER_HORIZONTAL;
        grid.setLayoutParams(glp);

        Button[] pads = new Button[4];
        for (int i = 0; i < 4; i++) {
            final int padIndex = i;
            Button pad = new Button(context);
            pad.setText(COLOR_NAMES[i]);
            pad.setTextColor(Color.WHITE);
            pad.setTypeface(Typeface.DEFAULT_BOLD);
            pad.setTextSize(16f);
            pad.setBackgroundColor(BASE_COLORS[i]);

            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.width = 240;
            param.height = 200;
            param.setMargins(12, 12, 12, 12);
            pad.setLayoutParams(param);

            // Tactile feedback
            AnimationHelper.attachBounceFeedback(pad);

            pad.setOnClickListener(v -> {
                if (isPlayingSequence) return;

                // Flash pad on tap
                flashPad(pad, padIndex, 250);

                int expectedIndex = userSequence.size();
                if (sequence.get(expectedIndex) == padIndex) {
                    userSequence.add(padIndex);
                    currentProgress = userSequence.size();
                    callback.onProgressUpdated(currentProgress, targetLength);
                    tvStep.setText("Step " + userSequence.size() + " of " + targetLength);

                    if (userSequence.size() == targetLength) {
                        tvStatus.setText("Pattern Complete! ☀️");
                        tvStatus.setTextColor(Color.GREEN);
                        callback.onChallengeSolved();
                    }
                } else {
                    // Mistake! Friendly replay
                    callback.onChallengeFailed("Try again! Watch the pattern replay...");
                    tvStatus.setText("Watch carefully...");
                    tvStatus.setTextColor(Color.parseColor("#FFD56B"));
                    userSequence.clear();
                    handler.postDelayed(() -> playSequence(pads, tvStatus), 1000);
                }
            });

            pads[i] = pad;
            grid.addView(pad);
        }

        root.addView(grid);

        // Start playing sequence after a brief pause
        handler.postDelayed(() -> playSequence(pads, tvStatus), 800);

        return root;
    }

    private void playSequence(Button[] pads, TextView tvStatus) {
        isPlayingSequence = true;
        tvStatus.setText("Watch the pattern...");
        tvStatus.setTextColor(Color.parseColor("#FFD56B"));
        userSequence.clear();

        for (int step = 0; step < sequence.size(); step++) {
            final int index = sequence.get(step);
            final int delay = (step + 1) * 700;
            final boolean isLast = (step == sequence.size() - 1);

            handler.postDelayed(() -> {
                flashPad(pads[index], index, 400);
                if (isLast) {
                    handler.postDelayed(() -> {
                        isPlayingSequence = false;
                        tvStatus.setText("Your Turn! Repeat the pattern ✨");
                        tvStatus.setTextColor(Color.parseColor("#3DDC84"));
                    }, 500);
                }
            }, delay);
        }
    }

    private void flashPad(Button pad, int index, int duration) {
        pad.setBackgroundColor(LIT_COLORS[index]);
        handler.postDelayed(() -> pad.setBackgroundColor(BASE_COLORS[index]), duration);
    }
}
