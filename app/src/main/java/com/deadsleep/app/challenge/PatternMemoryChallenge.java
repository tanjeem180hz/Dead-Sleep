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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simon-style pattern sequence memory challenge.
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

    private static final String[] COLOR_NAMES = {"RED", "BLUE", "GREEN", "YELLOW"};

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
                this.targetLength = 4;
                break;
            case HARD:
                this.targetLength = 8;
                break;
            case MEDIUM:
            default:
                this.targetLength = 6;
                break;
        }
        this.totalProblems = targetLength;
        generate();
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
        return "Pattern Memory Sequence";
    }

    @Override
    public String getInstructions() {
        return "Watch the " + targetLength + "-step color pattern, then repeat it.";
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
            pad.setTextSize(18f);
            pad.setBackgroundColor(BASE_COLORS[i]);

            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.width = 240;
            param.height = 200;
            param.setMargins(12, 12, 12, 12);
            pad.setLayoutParams(param);

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
                    // Mistake!
                    callback.onChallengeFailed("Wrong pad! Replaying sequence...");
                    tvStatus.setText("Incorrect! Watch carefully...");
                    tvStatus.setTextColor(Color.RED);
                    userSequence.clear();
                    handler.postDelayed(() -> playSequence(pads, tvStatus), 1200);
                }
            });

            pads[i] = pad;
            grid.addView(pad);
        }

        root.addView(grid);

        // Start playing sequence after 1 second delay
        handler.postDelayed(() -> playSequence(pads, tvStatus), 1000);

        return root;
    }

    private void playSequence(Button[] pads, TextView tvStatus) {
        isPlayingSequence = true;
        userSequence.clear();
        tvStatus.setText("Watch sequence...");
        tvStatus.setTextColor(Color.parseColor("#FFD56B"));

        for (int i = 0; i < sequence.size(); i++) {
            final int step = i;
            final int padIndex = sequence.get(step);
            handler.postDelayed(() -> {
                flashPad(pads[padIndex], padIndex, 500);
                if (step == sequence.size() - 1) {
                    handler.postDelayed(() -> {
                        isPlayingSequence = false;
                        tvStatus.setText("Your turn! Repeat the pattern.");
                        tvStatus.setTextColor(Color.WHITE);
                    }, 600);
                }
            }, (i + 1) * 800L);
        }
    }

    private void flashPad(Button pad, int index, long duration) {
        pad.setBackgroundColor(LIT_COLORS[index]);
        handler.postDelayed(() -> pad.setBackgroundColor(BASE_COLORS[index]), duration);
    }

    @Override
    public void cleanup() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
