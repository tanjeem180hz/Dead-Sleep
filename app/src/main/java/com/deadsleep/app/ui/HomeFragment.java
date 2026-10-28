package com.deadsleep.app.ui;

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
            long scheduleId = PreferencesManager.getInstance(requireContext()).getCurrentScheduleId();
            SleepModeManager.getInstance(requireContext()).startSleepMode(scheduleId);
            startActivity(new Intent(requireContext(), SleepModeActiveActivity.class));
        });

        binding.btnFixPermissions.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), PermissionWizardActivity.class));
        });

        binding.btnViewAdvice.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), RoutineAdvisorActivity.class));
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
        if (!isAdded()) return;

        PreferencesManager prefs = PreferencesManager.getInstance(requireContext());
        binding.tvHomeGreeting.setText("Welcome, " + prefs.getUserName() + " 🌙");

        // Permission health check
        boolean allGood = PermissionUtils.areAllCriticalPermissionsGranted(requireContext());
        binding.cardSetupHealth.setVisibility(allGood ? View.GONE : View.VISIBLE);

        // Streaks
        int streak = StatsManager.getInstance(requireContext()).getCurrentStreak();
        binding.tvStreakCount.setText(streak + " Day Streak");
        if (streak > 0) {
            binding.tvStreakSubtitle.setText("Keep your bedtime streak going tonight!");
        } else {
            binding.tvStreakSubtitle.setText("Solve wake-up challenge to start a streak");
        }

        // Active schedule details
        AppDatabase.getExecutor().execute(() -> {
            List<SleepSchedule> enabled = DeadSleepRepository.getInstance(requireContext()).getEnabledSchedulesSync();
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (enabled.isEmpty()) {
                        binding.tvNextScheduleDetail.setText("No active schedules. Tap Schedules tab to add one.");
                    } else {
                        SleepSchedule s = enabled.get(0);
                        binding.tvNextScheduleDetail.setText(s.getName() + ": " + s.getFormattedSleepTime() + " → " + s.getFormattedWakeTime());
                    }
                });
            }
        });
    }

    private void startCountdown() {
        stopCountdown();
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) return;
                long remainingMillis = ScheduleManager.getInstance(requireContext()).getMillisUntilNextSleep();
                if (remainingMillis <= 0) {
                    binding.tvCountdownTimer.setText("00:00");
                } else {
                    binding.tvCountdownTimer.setText(TimeUtils.formatCountdown(remainingMillis));
                }
                countdownHandler.postDelayed(this, 1000);
            }
        };
        countdownHandler.post(countdownRunnable);
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
        binding = null;
    }
}
