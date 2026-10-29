package com.deadsleep.app.ui;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.deadsleep.app.data.AppDatabase;
import com.deadsleep.app.data.DeadSleepRepository;
import com.deadsleep.app.databinding.ActivityAlarmRingingBinding;
import com.deadsleep.app.manager.DeadSleepAlarmManager;
import com.deadsleep.app.model.SleepSchedule;
import com.deadsleep.app.util.Constants;
import com.deadsleep.app.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Ringing alarm activity displayed directly over the lock screen.
 */
public class AlarmRingingActivity extends AppCompatActivity {

    private ActivityAlarmRingingBinding binding;
    private long scheduleId = -1;
    private String scheduleLabel = "Dead Sleep";
    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private Runnable timeRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupWindowFlags();

        binding = ActivityAlarmRingingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        scheduleId = getIntent().getLongExtra(Constants.EXTRA_SCHEDULE_ID, -1L);
        scheduleLabel = getIntent().getStringExtra(Constants.EXTRA_SCHEDULE_NAME);
        if (scheduleLabel != null) {
            binding.tvAlarmRingingLabel.setText(scheduleLabel + " ☀️");
        }

        setupSnoozeButton();

        binding.btnSolveChallenge.setOnClickListener(v -> {
            Intent challengeIntent = new Intent(this, ChallengeActivity.class);
            challengeIntent.putExtra(Constants.EXTRA_SCHEDULE_ID, scheduleId);
            startActivity(challengeIntent);
        });

        // Intercept back button - user cannot dismiss alarm by pressing back
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(AlarmRingingActivity.this, "Solve the challenge to turn off the alarm!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (km != null) km.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setupSnoozeButton() {
        AppDatabase.getExecutor().execute(() -> {
            SleepSchedule schedule = DeadSleepRepository.getInstance(this).getScheduleById(scheduleId);
            final int maxSnoozes = schedule != null ? schedule.getMaxSnoozeCount() : Constants.DEFAULT_MAX_SNOOZE_COUNT;
            final int snoozeDuration = schedule != null ? schedule.getSnoozeDurationMinutes() : Constants.DEFAULT_SNOOZE_DURATION_MINUTES;

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed() || binding == null) return;
                int currentSnoozes = DeadSleepAlarmManager.getInstance(this).getCurrentSnoozeCount();
                int remaining = Math.max(0, maxSnoozes - currentSnoozes);

                if (remaining <= 0) {
                    binding.btnSnoozeAlarm.setVisibility(View.GONE);
                } else {
                    binding.btnSnoozeAlarm.setText("Snooze " + snoozeDuration + "m (" + remaining + " left)");
                    binding.btnSnoozeAlarm.setOnClickListener(v -> {
                        boolean ok = DeadSleepAlarmManager.getInstance(this).snoozeAlarm(scheduleId, snoozeDuration, maxSnoozes);
                        if (ok) {
                            Toast.makeText(this, "Alarm snoozed for " + snoozeDuration + " minutes", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            binding.btnSnoozeAlarm.setVisibility(View.GONE);
                        }
                    });
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimeTicker();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimeTicker();
    }

    private void startTimeTicker() {
        stopTimeTicker();
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                if (isFinishing() || isDestroyed() || binding == null) return;
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm", Locale.getDefault());
                binding.tvAlarmRingingClock.setText(sdf.format(new Date()));
                timeHandler.postDelayed(this, 1000);
            }
        };
        timeHandler.post(timeRunnable);
    }

    private void stopTimeTicker() {
        if (timeRunnable != null) {
            timeHandler.removeCallbacks(timeRunnable);
            timeRunnable = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimeTicker();
    }
}
