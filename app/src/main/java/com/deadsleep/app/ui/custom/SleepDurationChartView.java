package com.deadsleep.app.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.deadsleep.app.manager.StatsManager;

/**
 * Custom Canvas chart rendering weekly sleep duration bars against target sleep line.
 */
public class SleepDurationChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint targetLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint targetTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();

    private StatsManager.ChartDataPoint[] dataPoints;
    private float maxHours = 12f;

    public SleepDurationChartView(Context context) {
        super(context);
        init();
    }

    public SleepDurationChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint.setColor(Color.parseColor("#9D85FF"));
        barPaint.setStyle(Paint.Style.FILL);

        targetLinePaint.setColor(Color.parseColor("#FFD56B"));
        targetLinePaint.setStyle(Paint.Style.STROKE);
        targetLinePaint.setStrokeWidth(4f);

        textPaint.setColor(Color.parseColor("#C7C5D0"));
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        targetTextPaint.setColor(Color.parseColor("#FFD56B"));
        targetTextPaint.setTextSize(24f);
        targetTextPaint.setTextAlign(Paint.Align.RIGHT);
    }

    public void setData(StatsManager.ChartDataPoint[] points) {
        this.dataPoints = points;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dataPoints == null || dataPoints.length == 0) return;

        int width = getWidth();
        int height = getHeight();
        int paddingBottom = 60;
        int paddingTop = 40;
        int paddingLeft = 40;
        int paddingRight = 40;

        float chartHeight = height - paddingBottom - paddingTop;
        float chartWidth = width - paddingLeft - paddingRight;

        int count = dataPoints.length;
        float slotWidth = chartWidth / count;
        float barWidth = slotWidth * 0.5f;

        // Draw 8h target line
        float targetY = paddingTop + (chartHeight * (1f - (8.0f / maxHours)));
        canvas.drawLine(paddingLeft, targetY, width - paddingRight, targetY, targetLinePaint);
        canvas.drawText("Target: 8h", width - paddingRight, targetY - 10, targetTextPaint);

        // Draw bars
        for (int i = 0; i < count; i++) {
            StatsManager.ChartDataPoint p = dataPoints[i];
            float cx = paddingLeft + (i * slotWidth) + (slotWidth / 2f);

            float barH = (p.actualValue / maxHours) * chartHeight;
            float top = (paddingTop + chartHeight) - barH;
            float bottom = paddingTop + chartHeight;

            barRect.set(cx - (barWidth / 2f), top, cx + (barWidth / 2f), bottom);
            canvas.drawRoundRect(barRect, 12f, 12f, barPaint);

            // Day label
            canvas.drawText(p.label, cx, height - 15, textPaint);

            // Duration label above bar
            String valStr = String.format("%.1fh", p.actualValue);
            canvas.drawText(valStr, cx, top - 12, textPaint);
        }
    }
}
