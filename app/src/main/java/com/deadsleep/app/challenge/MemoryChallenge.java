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
import java.util.Collections;
import java.util.List;

/**
 * Card-flip memory pair matching game.
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
    private final Handler handler = new Handler(Looper.getMainLooper());

    public MemoryChallenge(Difficulty difficulty) {
        super(difficulty);
        switch (difficulty) {
            case EASY:
                this.cols = 3;
                this.rows = 4;
                this.totalPairs = 6;
                break;
            case HARD:
                this.cols = 4;
                this.rows = 5;
                this.totalPairs = 10;
                break;
            case MEDIUM:
            default:
                this.cols = 4;
                this.rows = 4;
                this.totalPairs = 8;
                break;
        }
        this.totalProblems = totalPairs;
        generate();
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
            selectedSymbols.add(SYMBOLS[i % SYMBOLS.length]); // Add pair
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
        return "Card Flip Memory Challenge";
    }

    @Override
    public String getInstructions() {
        return "Find and match all " + totalPairs + " icon pairs to wake up.";
    }

    @Override
    public View renderView(Context context, ViewGroup container, ChallengeCallback callback) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(16, 12, 16, 12);

        TextView tvPairs = new TextView(context);
        tvPairs.setTextColor(Color.parseColor("#9D85FF"));
        tvPairs.setTextSize(14f);
        tvPairs.setTypeface(Typeface.DEFAULT_BOLD);
        tvPairs.setText("Pairs Matched: 0 / " + totalPairs);
        tvPairs.setGravity(Gravity.CENTER);
        tvPairs.setPadding(0, 0, 0, 16);
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

        // Dynamically size cards based on grid dimensions
        int cardWidth = (cols == 3) ? 180 : 150;
        int cardHeight = (rows == 5) ? 140 : 160;

        for (int i = 0; i < cards.size(); i++) {
            final Card card = cards.get(i);
            final Button btn = new Button(context);
            btn.setTextSize(26f);
            btn.setText("❓");
            btn.setBackgroundColor(Color.parseColor("#1C2237"));
            btn.setTextColor(Color.WHITE);

            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.width = cardWidth;
            param.height = cardHeight;
            param.setMargins(6, 6, 6, 6);
            btn.setLayoutParams(param);

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
                        firstSelected = null;
                        firstButton = null;
                        pairsMatched++;
                        currentProgress = pairsMatched;
                        tvPairs.setText("Pairs Matched: " + pairsMatched + " / " + totalPairs);
                        callback.onProgressUpdated(pairsMatched, totalPairs);
                        isBusy = false;

                        if (pairsMatched >= totalPairs) {
                            callback.onChallengeSolved();
                        }
                    } else {
                        // Mismatch
                        handler.postDelayed(() -> {
                            if (card != null) {
                                card.isFlipped = false;
                                btn.setText("❓");
                                btn.setBackgroundColor(Color.parseColor("#1C2237"));
                            }
                            if (firstSelected != null) {
                                firstSelected.isFlipped = false;
                                firstButton.setText("❓");
                                firstButton.setBackgroundColor(Color.parseColor("#1C2237"));
                            }
                            firstSelected = null;
                            firstButton = null;
                            isBusy = false;
                        }, 750);
                    }
                }
            });

            grid.addView(btn);
        }

        root.addView(grid);
        return root;
    }

    @Override
    public void cleanup() {
        handler.removeCallbacksAndMessages(null);
    }
}
