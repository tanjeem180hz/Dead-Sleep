package com.deadsleep.app.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Custom Canvas circular gauge displaying the 0-100 Sleep Consistency Score.
 */
public class ConsistencyGaugeView extends View {

    private final Paint bgArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint scoreArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textScorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();

    private int score = 85;

    public ConsistencyGaugeView(Context context) {
        super(context);
        init();
    }

    public ConsistencyGaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        bgArcPaint.setColor(Color.parseColor("#1C2237"));
        bgArcPaint.setStyle(Paint.Style.STROKE);
        bgArcPaint.setStrokeWidth(24f);
        bgArcPaint.setStrokeCap(Paint.Cap.ROUND);

        scoreArcPaint.setColor(Color.parseColor("#9D85FF"));
        scoreArcPaint.setStyle(Paint.Style.STROKE);
        scoreArcPaint.setStrokeWidth(24f);
        scoreArcPaint.setStrokeCap(Paint.Cap.ROUND);

        textScorePaint.setColor(Color.WHITE);
        textScorePaint.setTextSize(64f);
        textScorePaint.setTextAlign(Paint.Align.CENTER);
        textScorePaint.setFakeBoldText(true);

        textLabelPaint.setColor(Color.parseColor("#FFD56B"));
        textLabelPaint.setTextSize(26f);
        textLabelPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setScore(int score) {
        this.score = Math.max(0, Math.min(100, score));
        if (this.score >= 80) {
            scoreArcPaint.setColor(Color.parseColor("#69F0AE")); // Green
        } else if (this.score >= 60) {
            scoreArcPaint.setColor(Color.parseColor("#FFD56B")); // Yellow
        } else {
            scoreArcPaint.setColor(Color.parseColor("#FF5252")); // Red
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        float size = Math.min(width, height) * 0.75f;
        float cx = width / 2f;
        float cy = height / 2f;

        arcRect.set(cx - (size / 2f), cy - (size / 2f), cx + (size / 2f), cy + (size / 2f));

        // Draw background sweep arc (240 degrees)
        canvas.drawArc(arcRect, 150f, 240f, false, bgArcPaint);

        // Draw progress arc
        float sweep = (score / 100f) * 240f;
        canvas.drawArc(arcRect, 150f, sweep, false, scoreArcPaint);

        // Score text
        canvas.drawText(String.valueOf(score), cx, cy + 16, textScorePaint);

        // Label
        String label = (score >= 80) ? "Optimal" : (score >= 65 ? "Good" : "Fair");
        canvas.drawText(label, cx, cy + 60, textLabelPaint);
    }
}
