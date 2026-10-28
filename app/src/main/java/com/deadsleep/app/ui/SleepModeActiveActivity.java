package com.deadsleep.app.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.deadsleep.app.databinding.ActivitySleepModeActiveBinding;
import com.deadsleep.app.manager.ScheduleManager;
import com.deadsleep.app.manager.SleepModeManager;
import com.deadsleep.app.util.TimeUtils;

import java.util.Random;

/**
 * Full-screen calm sleep dashboard displaying wake countdown and the 10-second emergency unlock mechanism.
 */
public class SleepModeActiveActivity extends AppCompatActivity {

    private ActivitySleepModeActiveBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable countdownRunnable;

    private int holdProgress = 0;
    private Runnable holdRunnable;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySleepModeActiveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupEmergencyHold();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Do not allow exiting sleep mode via back button
                Toast.makeText(SleepModeActiveActivity.this, "Sleep mode is active. Sleep well! 🌙", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupEmergencyHold() {
        binding.btnEmergencyUnlock.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                holdProgress = 0;
                binding.pbEmergencyHold.setVisibility(View.VISIBLE);
                binding.pbEmergencyHold.setProgress(0);

                holdRunnable = new Runnable() {
                    @Override
                    public void run() {
                        holdProgress += 2;
                        binding.pbEmergencyHold.setProgress(holdProgress);

                        if (holdProgress >= 100) {
                            binding.pbEmergencyHold.setVisibility(View.INVISIBLE);
                            showEmergencyConfirmationDialog();
                        } else {
                            handler.postDelayed(this, 200); // 100 * 100ms = 10 seconds
                        }
                    }
                };
                handler.post(holdRunnable);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (holdRunnable != null) {
                    handler.removeCallbacks(holdRunnable);
                    holdRunnable = null;
                }
                binding.pbEmergencyHold.setVisibility(View.INVISIBLE);
                binding.pbEmergencyHold.setProgress(0);
                return true;
            }
            return false;
        });
    }

    private void showEmergencyConfirmationDialog() {
        Random rnd = new Random();
        int a = rnd.nextInt(20) + 10;
        int b = rnd.nextInt(20) + 10;
        int expected = a + b;

        final EditText etAnswer = new EditText(this);
        etAnswer.setHint("Answer: " + a + " + " + b);
        etAnswer.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(this)
                .setTitle("🚨 Emergency Unlock")
                .setMessage("Are you sure you need to exit Sleep Mode? Solve to confirm:\n\n" + a + " + " + b + " = ?")
                .setView(etAnswer)
                .setPositiveButton("Confirm Unlock", (dialog, which) -> {
                    String input = etAnswer.getText().toString().trim();
                    try {
                        int submitted = Integer.parseInt(input);
                        if (submitted == expected) {
                            SleepModeManager.getInstance(this).emergencyUnlock();
                            Toast.makeText(this, "Emergency exit processed. Sleep mode ended.", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Incorrect confirmation answer.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid number.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCountdown();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCountdown();
    }

    private void startCountdown() {
        stopCountdown();
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                long remainingMillis = ScheduleManager.getInstance(SleepModeActiveActivity.this).getMillisUntilNextSleep();
                binding.tvActiveCountdown.setText(TimeUtils.formatCountdown(remainingMillis));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(countdownRunnable);
    }

    private void stopCountdown() {
        if (countdownRunnable != null) {
            handler.removeCallbacks(countdownRunnable);
            countdownRunnable = null;
        }
    }
}
