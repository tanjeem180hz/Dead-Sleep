package com.deadsleep.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.deadsleep.app.data.AppDatabase;
import com.deadsleep.app.data.DeadSleepRepository;
import com.deadsleep.app.data.PreferencesManager;
import com.deadsleep.app.databinding.FragmentHomeBinding;
import com.deadsleep.app.manager.ScheduleManager;
import com.deadsleep.app.manager.SleepModeManager;
import com.deadsleep.app.manager.StatsManager;
import com.deadsleep.app.model.SleepSchedule;
import com.deadsleep.app.util.PermissionUtils;
import com.deadsleep.app.util.TimeUtils;

import java.util.List;

/**
 * Dashboard screen with next sleep countdown, manual sleep mode toggle, streak, and health alerts.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private final Handler countdownHandler = new Handler(Looper.getMainLooper());
    private Runnable countdownRunnable;
    private long cachedNextSleepTime = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnStartSleepNow.setOnClickListener(v -> {
            Context ctx = getContext();
            if (ctx == null) return;
            long scheduleId = PreferencesManager.getInstance(ctx).getCurrentScheduleId();
            SleepModeManager.getInstance(ctx).startSleepMode(scheduleId);
            startActivity(new Intent(ctx, SleepModeActiveActivity.class));
        });

        binding.btnFixPermissions.setOnClickListener(v -> {
            Context ctx = getContext();
            if (ctx == null) return;
            startActivity(new Intent(ctx, PermissionWizardActivity.class));
        });

        binding.btnViewAdvice.setOnClickListener(v -> {
            Context ctx = getContext();
            if (ctx == null) return;
            startActivity(new Intent(ctx, RoutineAdvisorActivity.class));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
        startCountdown();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCountdown();
    }

    private void updateUI() {
        Context ctx = getContext();
        if (ctx == null || !isAdded() || binding == null) return;

        PreferencesManager prefs = PreferencesManager.getInstance(ctx);
        binding.tvHomeGreeting.setText("Welcome, " + prefs.getUserName() + " 🌙");

        // Permission health check
        boolean allGood = PermissionUtils.areAllCriticalPermissionsGranted(ctx);
        binding.cardSetupHealth.setVisibility(allGood ? View.GONE : View.VISIBLE);

        // Streaks
        int streak = StatsManager.getInstance(ctx).getCurrentStreak();
        binding.tvStreakCount.setText(streak + " Day Streak");
        if (streak > 0) {
            binding.tvStreakSubtitle.setText("Keep your bedtime streak going tonight!");
        } else {
            binding.tvStreakSubtitle.setText("Solve wake-up challenge to start a streak");
        }

        // Active schedule details & Next sleep calculation on background thread
        AppDatabase.getExecutor().execute(() -> {
            try {
                Context bgCtx = getContext();
                if (bgCtx == null) return;
                List<SleepSchedule> enabled = DeadSleepRepository.getInstance(bgCtx).getEnabledSchedulesSync();
                long remainingMillis = ScheduleManager.getInstance(bgCtx).getMillisUntilNextSleep();
                cachedNextSleepTime = remainingMillis > 0 ? (System.currentTimeMillis() + remainingMillis) : -1;

                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (binding == null) return;
                        if (enabled == null || enabled.isEmpty()) {
                            binding.tvNextScheduleDetail.setText("No active schedules. Tap Schedules tab to add one.");
                        } else {
                            SleepSchedule s = enabled.get(0);
                            binding.tvNextScheduleDetail.setText(s.getName() + ": " + s.getFormattedSleepTime() + " → " + s.getFormattedWakeTime());
                        }
                        updateCountdownText();
                    });
                }
            } catch (Exception ignored) {
            }
        });
    }

    private void startCountdown() {
        stopCountdown();
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded() || binding == null) return;
                updateCountdownText();
                countdownHandler.postDelayed(this, 1000);
            }
        };
        countdownHandler.post(countdownRunnable);
    }

    private void updateCountdownText() {
        if (binding == null) return;
        if (cachedNextSleepTime <= 0) {
            binding.tvCountdownTimer.setText("00:00");
        } else {
            long remaining = cachedNextSleepTime - System.currentTimeMillis();
            if (remaining <= 0) {
                binding.tvCountdownTimer.setText("00:00");
            } else {
                binding.tvCountdownTimer.setText(TimeUtils.formatCountdown(remaining));
            }
        }
    }

    private void stopCountdown() {
        if (countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
            countdownRunnable = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopCountdown();
        binding = null;
    }
}
