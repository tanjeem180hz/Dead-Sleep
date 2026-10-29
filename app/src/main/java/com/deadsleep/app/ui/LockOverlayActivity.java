package com.deadsleep.app.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.deadsleep.app.databinding.ActivityLockOverlayBinding;
import com.deadsleep.app.manager.ScheduleManager;
import com.deadsleep.app.util.Constants;
import com.deadsleep.app.util.TimeUtils;

/**
 * Full-screen lockout overlay shown when a user opens a restricted app during sleep mode.
 */
public class LockOverlayActivity extends AppCompatActivity {

    private ActivityLockOverlayBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLockOverlayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String blockedPackage = getIntent().getStringExtra(Constants.EXTRA_BLOCKED_PACKAGE);
        if (blockedPackage != null) {
            binding.tvBlockedAppName.setText(blockedPackage);
        }

        binding.tvLockRemaining.setText("Sleep Mode is Active 🌙");

        com.deadsleep.app.data.AppDatabase.getExecutor().execute(() -> {
            try {
                com.deadsleep.app.model.SleepSession session = com.deadsleep.app.data.DeadSleepRepository.getInstance(this).getActiveSessionSync();
                long targetWake = (session != null && session.getTargetWakeTimeMillis() > 0) ? session.getTargetWakeTimeMillis() : -1;
                if (targetWake <= 0) {
                    long schedId = com.deadsleep.app.data.PreferencesManager.getInstance(this).getCurrentScheduleId();
                    com.deadsleep.app.model.SleepSchedule schedule = com.deadsleep.app.data.DeadSleepRepository.getInstance(this).getScheduleById(schedId);
                    if (schedule != null) {
                        targetWake = TimeUtils.getNextOccurrenceMillis(
                                schedule.getWakeHour(), schedule.getWakeMinute(), schedule.getDaysOfWeek()
                        );
                    }
                }
                final long remaining = targetWake > 0 ? Math.max(0, targetWake - System.currentTimeMillis()) : 0;
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed() || binding == null) return;
                    if (remaining > 0) {
                        binding.tvLockRemaining.setText("Wake-up in: " + TimeUtils.formatCountdown(remaining));
                    }
                });
            } catch (Exception ignored) {}
        });

        binding.btnGoToSleep.setOnClickListener(v -> sendToHome());

        // Intercept back button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                sendToHome();
            }
        });
    }

    private void sendToHome() {
        try {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
        } catch (Exception ignored) {}
        finish();
    }
}
