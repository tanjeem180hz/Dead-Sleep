package com.deadsleep.app.challenge;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.deadsleep.app.R;
import com.deadsleep.app.util.AnimationHelper;

/**
 * Physical shake-to-wake challenge utilizing the device accelerometer.
 * Scaled to require comfortable, gentle shaking rather than violent shaking.
 */
public class ShakeChallenge extends Challenge implements SensorEventListener {

    private final int requiredShakes;
    private int currentShakes = 0;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float lastX, lastY, lastZ;
    private long lastUpdate = 0;
    private static final int SHAKE_THRESHOLD = 600; // Lowered from 800 for comfortable, smooth registration

    private TextView tvCount;
    private ProgressBar progressBar;
    private ChallengeCallback callback;
    private TextView tvIcon;

    public ShakeChallenge(Difficulty difficulty) {
        super(difficulty);
        switch (difficulty) {
            case EASY:
                this.requiredShakes = 10; // Gentle 10 shakes for Easy
                break;
            case HARD:
                this.requiredShakes = 32;
                break;
            case MEDIUM:
            default:
                this.requiredShakes = 18; // Friendly 18 shakes for Medium (takes only ~6s)
                break;
        }
        this.totalProblems = requiredShakes;
        generate();
    }

    @Override
    public String getIconEmoji() {
        return "📳";
    }

    @Override
    public int getIconDrawableRes() {
        return R.drawable.ic_challenge_shake;
    }

    public static boolean isSensorAvailable(Context context) {
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        return sm != null && sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null;
    }

    @Override
    public void generate() {
        currentShakes = 0;
        currentProgress = 0;
    }

    @Override
    public boolean validate(Object answer) {
        return currentShakes >= requiredShakes;
    }

    @Override
    public String getTitle() {
        return "📳 Shake-to-Wake Challenge";
    }

    @Override
    public String getInstructions() {
        return "Gently shake your phone " + requiredShakes + " times to dismiss the alarm!";
    }

    @Override
    public View renderView(Context context, ViewGroup container, ChallengeCallback callback) {
        this.callback = callback;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            }
        }

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(24, 24, 24, 24);

        CardView card = new CardView(context);
        card.setCardBackgroundColor(Color.parseColor("#181D30"));
        card.setRadius(28f);
        card.setCardElevation(8f);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        card.setLayoutParams(cardParams);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(32, 40, 32, 40);

        tvIcon = new TextView(context);
        tvIcon.setText("📱💨");
        tvIcon.setTextSize(50f);
        tvIcon.setGravity(Gravity.CENTER);
        content.addView(tvIcon);

        tvCount = new TextView(context);
        tvCount.setText("0 / " + requiredShakes);
        tvCount.setTextColor(Color.parseColor("#FFD56B"));
        tvCount.setTextSize(38f);
        tvCount.setTypeface(Typeface.DEFAULT_BOLD);
        tvCount.setGravity(Gravity.CENTER);
        tvCount.setPadding(0, 16, 0, 16);
        content.addView(tvCount);

        progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(requiredShakes);
        progressBar.setProgress(0);
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3DDC84")));
        LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 26
        );
        progressBar.setLayoutParams(pbParams);
        content.addView(progressBar);

        TextView tvHint = new TextView(context);
        tvHint.setText("Gently shake to wake your body ✨");
        tvHint.setTextColor(Color.parseColor("#9D85FF"));
        tvHint.setTextSize(14f);
        tvHint.setGravity(Gravity.CENTER);
        tvHint.setPadding(0, 16, 0, 0);
        content.addView(tvHint);

        if (accelerometer == null) {
            tvHint.setText("Sensor not detected. Tap button repeatedly to wake:");
            Button btnTap = new Button(context);
            btnTap.setText("Tap to Wake ⚡");
            btnTap.setBackgroundColor(Color.parseColor("#35248A"));
            btnTap.setTextColor(Color.WHITE);
            AnimationHelper.attachBounceFeedback(btnTap);
            btnTap.setOnClickListener(v -> recordShake());
            content.addView(btnTap);
        }

        card.addView(content);
        root.addView(card);

        return root;
    }

    private void recordShake() {
        currentShakes++;
        currentProgress = currentShakes;

        if (tvCount != null) {
            tvCount.setText(currentShakes + " / " + requiredShakes);
        }
        if (progressBar != null) {
            progressBar.setProgress(currentShakes);
        }
        if (tvIcon != null) {
            AnimationHelper.pop(tvIcon);
        }
        if (callback != null) {
            callback.onProgressUpdated(currentShakes, requiredShakes);
        }

        if (currentShakes >= requiredShakes) {
            cleanup();
            if (callback != null) {
                callback.onChallengeSolved();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;
                if (speed > SHAKE_THRESHOLD) {
                    recordShake();
                }

                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void cleanup() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            sensorManager = null;
        }
    }
}
