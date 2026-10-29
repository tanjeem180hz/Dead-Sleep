package com.deadsleep.app.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.deadsleep.app.databinding.ActivityAlarmTestBinding;
import com.deadsleep.app.manager.DeadSleepAlarmManager;

/**
 * 10-second test countdown to verify background alarms, lock-screen firing, and challenge puzzles.
 */
public class AlarmTestActivity extends AppCompatActivity {

    private ActivityAlarmTestBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int secondsRemaining = 10;
    private Runnable countdownRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlarmTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DeadSleepAlarmManager.getInstance(this).triggerTestAlarm(10);

        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                if (isFinishing() || isDestroyed() || binding == null) return;
                secondsRemaining--;
                if (secondsRemaining > 0) {
                    binding.tvTestCountdown.setText("Ringing in " + secondsRemaining + "s...");
                    handler.postDelayed(this, 1000);
                } else {
                    binding.tvTestCountdown.setText("Ringing NOW!");
                }
            }
        };
        handler.postDelayed(countdownRunnable, 1000);

        binding.btnCancelTest.setOnClickListener(v -> {
            DeadSleepAlarmManager.getInstance(this).stopAlarmSound();
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countdownRunnable != null) {
            handler.removeCallbacks(countdownRunnable);
        }
    }
}
