package com.deadsleep.app.challenge;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Arithmetic challenge scaling from simple sums to multi-step equations with a custom keypad.
 */
public class MathChallenge extends Challenge {

    private static class MathProblem {
        String expression;
        int answer;

        MathProblem(String expression, int answer) {
            this.expression = expression;
            this.answer = answer;
        }
    }

    private final List<MathProblem> problems = new ArrayList<>();
    private final Random random = new Random();
    private int currentIndex = 0;

    public MathChallenge(Difficulty difficulty) {
        super(difficulty);
        switch (difficulty) {
            case EASY:
                this.totalProblems = 1;
                break;
            case HARD:
                this.totalProblems = 5;
                break;
            case MEDIUM:
            default:
                this.totalProblems = 3;
                break;
        }
        generate();
    }

    @Override
    public void generate() {
        problems.clear();
        currentIndex = 0;
        currentProgress = 0;

        for (int i = 0; i < totalProblems; i++) {
            problems.add(createProblem(difficulty));
        }
    }

    private MathProblem createProblem(Difficulty diff) {
        if (diff == Difficulty.EASY) {
            int a = random.nextInt(40) + 10;
            int b = random.nextInt(40) + 10;
            if (random.nextBoolean()) {
                return new MathProblem(a + " + " + b, a + b);
            } else {
                int max = Math.max(a, b);
                int min = Math.min(a, b);
                return new MathProblem(max + " - " + min, max - min);
            }
        } else if (diff == Difficulty.MEDIUM) {
            int op = random.nextInt(3);
            if (op == 0) {
                // a * b + c
                int a = random.nextInt(9) + 3;
                int b = random.nextInt(9) + 2;
                int c = random.nextInt(30) + 5;
                return new MathProblem(a + " × " + b + " + " + c, (a * b) + c);
            } else if (op == 1) {
                // a * b - c
                int a = random.nextInt(9) + 3;
                int b = random.nextInt(9) + 2;
                int c = random.nextInt(15) + 2;
                return new MathProblem(a + " × " + b + " - " + c, (a * b) - c);
            } else {
                // a + b + c
                int a = random.nextInt(50) + 20;
                int b = random.nextInt(50) + 20;
                int c = random.nextInt(30) + 10;
                return new MathProblem(a + " + " + b + " + " + c, a + b + c);
            }
        } else {
            // HARD: brackets or larger multiplication
            int op = random.nextInt(2);
            if (op == 0) {
                int a = random.nextInt(20) + 10;
                int b = random.nextInt(15) + 5;
                int c = random.nextInt(5) + 2;
                return new MathProblem("(" + a + " + " + b + ") × " + c, (a + b) * c);
            } else {
                int a = random.nextInt(15) + 11;
                int b = random.nextInt(9) + 4;
                int c = random.nextInt(40) + 20;
                return new MathProblem(a + " × " + b + " - " + c, (a * b) - c);
            }
        }
    }

    @Override
    public boolean validate(Object answer) {
        if (!(answer instanceof Integer) || problems.isEmpty() || currentIndex >= problems.size()) {
            return false;
        }
        return problems.get(currentIndex).answer == (int) answer;
    }

    @Override
    public String getTitle() {
        return "Math Arithmetic Challenge";
    }

    @Override
    public String getInstructions() {
        return "Solve " + totalProblems + " arithmetic problems to wake up.";
    }

    @Override
    public View renderView(Context context, ViewGroup container, ChallengeCallback callback) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(24, 16, 24, 16);

        // Progress Text
        TextView tvProgress = new TextView(context);
        tvProgress.setTextColor(Color.parseColor("#9D85FF"));
        tvProgress.setTextSize(14f);
        tvProgress.setTypeface(Typeface.DEFAULT_BOLD);
        tvProgress.setText("Problem " + (currentIndex + 1) + " of " + totalProblems);
        tvProgress.setGravity(Gravity.CENTER);
        root.addView(tvProgress);

        // Expression Card
        CardView card = new CardView(context);
        card.setCardBackgroundColor(Color.parseColor("#181D30"));
        card.setRadius(24f);
        card.setCardElevation(8f);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 16, 0, 16);
        card.setLayoutParams(cardParams);

        LinearLayout cardContent = new LinearLayout(context);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(32, 28, 32, 28);
        cardContent.setGravity(Gravity.CENTER);

        TextView tvExpression = new TextView(context);
        tvExpression.setText(problems.get(currentIndex).expression);
        tvExpression.setTextColor(Color.WHITE);
        tvExpression.setTextSize(34f);
        tvExpression.setTypeface(Typeface.DEFAULT_BOLD);
        tvExpression.setGravity(Gravity.CENTER);
        cardContent.addView(tvExpression);

        TextView tvInput = new TextView(context);
        tvInput.setText("?");
        tvInput.setTextColor(Color.parseColor("#FFD56B"));
        tvInput.setTextSize(38f);
        tvInput.setTypeface(Typeface.DEFAULT_BOLD);
        tvInput.setGravity(Gravity.CENTER);
        tvInput.setPadding(0, 12, 0, 0);
        cardContent.addView(tvInput);

        card.addView(cardContent);
        root.addView(card);

        // Keypad Container
        GridLayout keypad = new GridLayout(context);
        keypad.setColumnCount(3);
        keypad.setRowCount(4);
        keypad.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        LinearLayout.LayoutParams kpParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        kpParams.gravity = Gravity.CENTER_HORIZONTAL;
        keypad.setLayoutParams(kpParams);

        StringBuilder currentInput = new StringBuilder();

        String[] keys = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫", "0", "✓"};

        for (String key : keys) {
            Button btn = new Button(context);
            btn.setText(key);
            btn.setTextSize(22f);
            btn.setTypeface(Typeface.DEFAULT_BOLD);
            btn.setTextColor(Color.WHITE);

            if ("✓".equals(key)) {
                btn.setBackgroundColor(Color.parseColor("#35248A"));
                btn.setTextColor(Color.parseColor("#FFE08D"));
            } else if ("⌫".equals(key)) {
                btn.setBackgroundColor(Color.parseColor("#262B40"));
                btn.setTextColor(Color.parseColor("#FFB4AB"));
            } else {
                btn.setBackgroundColor(Color.parseColor("#1C2237"));
            }

            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.width = 180;
            param.height = 130;
            param.setMargins(10, 10, 10, 10);
            btn.setLayoutParams(param);

            btn.setOnClickListener(v -> {
                if ("⌫".equals(key)) {
                    if (currentInput.length() > 0) {
                        currentInput.deleteCharAt(currentInput.length() - 1);
                    }
                    tvInput.setText(currentInput.length() == 0 ? "?" : currentInput.toString());
                } else if ("✓".equals(key)) {
                    if (currentInput.length() == 0) return;
                    try {
                        int submitted = Integer.parseInt(currentInput.toString());
                        if (validate(submitted)) {
                            currentIndex++;
                            currentProgress++;
                            callback.onProgressUpdated(currentProgress, totalProblems);

                            if (currentIndex >= totalProblems) {
                                callback.onChallengeSolved();
                            } else {
                                tvProgress.setText("Problem " + (currentIndex + 1) + " of " + totalProblems);
                                tvExpression.setText(problems.get(currentIndex).expression);
                                currentInput.setLength(0);
                                tvInput.setText("?");
                            }
                        } else {
                            callback.onChallengeFailed("Incorrect answer! Generating new problem.");
                            currentInput.setLength(0);
                            tvInput.setText("?");
                            problems.set(currentIndex, createProblem(difficulty));
                            tvExpression.setText(problems.get(currentIndex).expression);
                        }
                    } catch (NumberFormatException ignored) {}
                } else {
                    if (currentInput.length() < 6) {
                        currentInput.append(key);
                        tvInput.setText(currentInput.toString());
                    }
                }
            });

            keypad.addView(btn);
        }

        root.addView(keypad);
        return root;
    }
}
