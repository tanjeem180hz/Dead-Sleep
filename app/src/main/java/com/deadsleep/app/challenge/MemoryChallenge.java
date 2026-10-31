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
import java.util.Collections;
import java.util.List;

/**
 * Friendly card-flip memory pair matching game.
 * Scaled to be comfortable, relaxing, and quick to solve in the morning.
 */
public class MemoryChallenge extends Challenge {

    private static class Card {
        int id;
        String symbol;
        boolean isMatched;
        boolean isFlipped;

        Card(int id, String symbol) {
            this.id = id;
            this.symbol = symbol;
            this.isMatched = false;
            this.isFlipped = false;
        }
    }

    private static final String[] SYMBOLS = {
            "🌙", "⭐", "🦉", "⏰", "🛌", "✨", "🪐", "🕯️", "🌌", "☕"
    };

    private final List<Card> cards = new ArrayList<>();
    private int rows;
    private int cols;
    private int pairsMatched = 0;
    private int totalPairs;

    private Card firstSelected = null;
    private Button firstButton = null;
    private boolean isBusy = false;
    private Handler handler;

    public MemoryChallenge(Difficulty difficulty) {
        super(difficulty);
        switch (difficulty) {
            case EASY:
                this.cols = 2;
                this.rows = 2;
                this.totalPairs = 2; // 4 cards total (2 pairs) - super easy & friendly
                break;
            case HARD:
                this.cols = 4;
                this.rows = 2;
                this.totalPairs = 4; // 8 cards total (4 pairs)
                break;
            case MEDIUM:
            default:
                this.cols = 3;
                this.rows = 2;
                this.totalPairs = 3; // 6 cards total (3 pairs) - friendly, gentle wake-up
                break;
        }
        this.totalProblems = totalPairs;
        generate();
    }

    @Override
    public String getIconEmoji() {
        return "🃏";
    }

    @Override
    public int getIconDrawableRes() {
        return R.drawable.ic_challenge_memory;
    }

    @Override
    public void generate() {
        cards.clear();
        pairsMatched = 0;
        currentProgress = 0;
        firstSelected = null;
        firstButton = null;
        isBusy = false;

        List<String> selectedSymbols = new ArrayList<>();
        for (int i = 0; i < totalPairs; i++) {
            selectedSymbols.add(SYMBOLS[i % SYMBOLS.length]);
            selectedSymbols.add(SYMBOLS[i % SYMBOLS.length]); // Add matching pair
        }
        Collections.shuffle(selectedSymbols);

        for (int i = 0; i < selectedSymbols.size(); i++) {
            cards.add(new Card(i, selectedSymbols.get(i)));
        }
    }

    @Override
    public boolean validate(Object answer) {
        return pairsMatched >= totalPairs;
    }

    @Override
    public String getTitle() {
        return "🃏 Memory Match Challenge";
    }

    @Override
    public String getInstructions() {
        return "Find all " + totalPairs + " matching pairs of bedtime symbols to wake up.";
    }

    @Override
    public View renderView(Context context, ViewGroup container, ChallengeCallback callback) {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(16, 16, 16, 16);

        TextView tvPairs = new TextView(context);
        tvPairs.setTextColor(Color.parseColor("#9D85FF"));
        tvPairs.setTextSize(15f);
        tvPairs.setTypeface(Typeface.DEFAULT_BOLD);
        tvPairs.setText("Pairs Matched: 0 / " + totalPairs);
        tvPairs.setGravity(Gravity.CENTER);
        tvPairs.setPadding(0, 0, 0, 20);
        root.addView(tvPairs);

        GridLayout grid = new GridLayout(context);
        grid.setColumnCount(cols);
        grid.setRowCount(rows);
        grid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        glp.gravity = Gravity.CENTER_HORIZONTAL;
        grid.setLayoutParams(glp);

        // Generous, comfortable card dimensions
        int cardWidth = (cols == 2) ? 240 : (cols == 3 ? 200 : 170);
        int cardHeight = (cols == 2) ? 240 : 200;

        for (int i = 0; i < cards.size(); i++) {
            final Card card = cards.get(i);
            final Button btn = new Button(context);
            btn.setTextSize(32f);
            btn.setText("❓");
            btn.setBackgroundColor(Color.parseColor("#1C2237"));
            btn.setTextColor(Color.WHITE);

            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.width = cardWidth;
            param.height = cardHeight;
            param.setMargins(8, 8, 8, 8);
            btn.setLayoutParams(param);

            // Tactile feedback
            AnimationHelper.attachBounceFeedback(btn);

            btn.setOnClickListener(v -> {
                if (isBusy || card.isMatched || card.isFlipped) return;

                card.isFlipped = true;
                btn.setText(card.symbol);
                btn.setBackgroundColor(Color.parseColor("#35248A"));

                if (firstSelected == null) {
                    firstSelected = card;
                    firstButton = btn;
                } else {
                    isBusy = true;
                    if (firstSelected.symbol.equals(card.symbol)) {
                        // Match!
                        card.isMatched = true;
                        firstSelected.isMatched = true;
                        btn.setBackgroundColor(Color.parseColor("#1E382B"));
                        firstButton.setBackgroundColor(Color.parseColor("#1E382B"));
                        AnimationHelper.pop(btn);
                        AnimationHelper.pop(firstButton);

                        firstSelected = null;
                        firstButton = null;
                        pairsMatched++;
                        currentProgress = pairsMatched;
                        callback.onProgressUpdated(pairsMatched, totalPairs);

                        tvPairs.setText("Pairs Matched: " + pairsMatched + " / " + totalPairs + " 🎉");
                        isBusy = false;

                        if (pairsMatched >= totalPairs) {
                            handler.postDelayed(callback::onChallengeSolved, 300);
                        }
                    } else {
                        // Mismatch: flip back after brief friendly pause
                        final Card pendingFirst = firstSelected;
                        final Button pendingFirstBtn = firstButton;
                        firstSelected = null;
                        firstButton = null;

                        handler.postDelayed(() -> {
                            card.isFlipped = false;
                            pendingFirst.isFlipped = false;
                            btn.setText("❓");
                            btn.setBackgroundColor(Color.parseColor("#1C2237"));
                            pendingFirstBtn.setText("❓");
                            pendingFirstBtn.setBackgroundColor(Color.parseColor("#1C2237"));
                            isBusy = false;
                        }, 700);
                    }
                }
            });

            grid.addView(btn);
        }

        root.addView(grid);
        return root;
    }
}
