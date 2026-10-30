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
import com.deadsleep.app.model.SleepSession;
import com.deadsleep.app.util.PermissionUtils;
import com.deadsleep.app.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Dashboard screen with real-time live device clock, next sleep countdown, manual sleep mode toggle, streak, and health alerts.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private final Handler tickerHandler = new Handler(Looper.getMainLooper());
    private Runnable tickerRunnable;

    private long cachedTargetTime = -1;
    private boolean isSleepActive = false;

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
            if (SleepModeManager.getInstance(ctx).isSleepModeActive()) {
                startActivity(new Intent(ctx, SleepModeActiveActivity.class));
            } else {
                long scheduleId = PreferencesManager.getInstance(ctx).getCurrentScheduleId();
                SleepModeManager.getInstance(ctx).startSleepMode(scheduleId);
                startActivity(new Intent(ctx, SleepModeActiveActivity.class));
            }
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

        binding.btnOpenBedtimeSection.setOnClickListener(v -> {
            Context ctx = getContext();
            if (ctx == null) return;
            startActivity(new Intent(ctx, BedtimeActivity.class));
        });

        binding.cardBedtimeMode.setOnClickListener(v -> {
            Context ctx = getContext();
            if (ctx == null) return;
            startActivity(new Intent(ctx, BedtimeActivity.class));
        });

        binding.switchHomeAutoBedtime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Context ctx = getContext();
            if (ctx == null) return;
            PreferencesManager.getInstance(ctx).setBedtimeModeAutoEnabled(isChecked);
            updateBedtimeSummary();
        });

        binding.switchHomeGrayscale.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Context ctx = getContext();
            if (ctx == null) return;
            PreferencesManager.getInstance(ctx).setBedtimeGrayscaleEnabled(isChecked);
            updateBedtimeSummary();
        });
    }

    private void updateBedtimeSummary() {
        if (binding == null) return;
        Context ctx = getContext();
        if (ctx == null) return;
        PreferencesManager prefs = PreferencesManager.getInstance(ctx);
        boolean auto = prefs.isBedtimeModeAutoEnabled();
        boolean gray = prefs.isBedtimeGrayscaleEnabled();
        String summary = (auto ? "Auto Bedtime: ON" : "Auto Bedtime: OFF") + " • " + (gray ? "B&W Display: ON" : "B&W Display: OFF");
        binding.tvBedtimeSummary.setText(summary);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
        startTicker();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTicker();
    }

    private void updateUI() {
        Context ctx = getContext();
        if (ctx == null || !isAdded() || binding == null) return;

        PreferencesManager prefs = PreferencesManager.getInstance(ctx);
        String name = prefs.getUserName();
        if (name == null || name.trim().isEmpty()) name = "Friend";
        binding.tvHomeGreeting.setText("Welcome, " + name + " 🌙");

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

        isSleepActive = SleepModeManager.getInstance(ctx).isSleepModeActive();
        if (isSleepActive) {
            binding.tvCountdownTitle.setText("SLEEP MODE ACTIVE 🌙");
            binding.btnStartSleepNow.setText("🌙 View Sleep Dashboard");
        } else {
            binding.tvCountdownTitle.setText("NEXT SLEEP MODE STARTS IN");
            binding.btnStartSleepNow.setText("🌙 Start Sleep Mode Now");
        }

        // Bedtime Mode switches
        binding.switchHomeAutoBedtime.setChecked(prefs.isBedtimeModeAutoEnabled());
        binding.switchHomeGrayscale.setChecked(prefs.isBedtimeGrayscaleEnabled());
        updateBedtimeSummary();

        // Active schedule details & Next sleep/wake calculation on background thread
        AppDatabase.getExecutor().execute(() -> {
            try {
                Context bgCtx = getContext();
                if (bgCtx == null) return;

                if (isSleepActive) {
                    SleepSession session = DeadSleepRepository.getInstance(bgCtx).getActiveSessionSync();
                    long targetWake = (session != null && session.getTargetWakeTimeMillis() > System.currentTimeMillis())
                            ? session.getTargetWakeTimeMillis() : -1;
                    if (targetWake <= 0) {
                        SleepSchedule nextSched = ScheduleManager.getInstance(bgCtx).getNextActiveSchedule();
                        if (nextSched != null) {
                            targetWake = TimeUtils.getTargetWakeTimeMillis(
                                    System.currentTimeMillis(), nextSched.getWakeHour(), nextSched.getWakeMinute()
                            );
                        }
                    }
                    cachedTargetTime = targetWake;
                    final long finalTargetWake = targetWake;

                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (binding == null) return;
                            if (finalTargetWake > 0) {
                                java.util.Calendar cal = java.util.Calendar.getInstance();
                                cal.setTimeInMillis(finalTargetWake);
                                String wakeStr = TimeUtils.formatTime(bgCtx, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE));
                                binding.tvNextScheduleDetail.setText("Wake-up Alarm at " + wakeStr);
                            } else {
                                binding.tvNextScheduleDetail.setText("Rest well! Alarm will sound in the morning.");
                            }
                            updateTickerDisplay();
                        });
                    }
                } else {
                    SleepSchedule nextSched = ScheduleManager.getInstance(bgCtx).getNextActiveSchedule();
                    long remainingMillis = ScheduleManager.getInstance(bgCtx).getMillisUntilNextSleep();
                    cachedTargetTime = remainingMillis > 0 ? (System.currentTimeMillis() + remainingMillis) : -1;

                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (binding == null) return;
                            if (nextSched == null) {
                                binding.tvNextScheduleDetail.setText("No active schedules. Tap Schedules tab to add one.");
                            } else {
                                binding.tvNextScheduleDetail.setText(nextSched.getName() + ": "
                                        + nextSched.getFormattedSleepTime(bgCtx) + " → "
                                        + nextSched.getFormattedWakeTime(bgCtx));
                            }
                            updateTickerDisplay();
                        });
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }

    private void updateBedtimeSummary() {
        if (binding == null) return;
        Context ctx = getContext();
        if (ctx == null) return;
        PreferencesManager prefs = PreferencesManager.getInstance(ctx);
        boolean auto = prefs.isBedtimeModeAutoEnabled();
        boolean gray = prefs.isBedtimeGrayscaleEnabled();
        String summary = (auto ? "Auto Bedtime: ON" : "Auto Bedtime: OFF") + " • " + (gray ? "B&W Display: ON" : "B&W Display: OFF");
        binding.tvBedtimeSummary.setText(summary);
    }

    private void startTicker() {
        stopTicker();
        tickerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded() || binding == null) return;
                updateTickerDisplay();
                tickerHandler.postDelayed(this, 1000);
            }
        };
        tickerHandler.post(tickerRunnable);
    }

    private void updateTickerDisplay() {
        if (binding == null) return;
        Context ctx = getContext();
        if (ctx == null) return;

        // 1. Update Live Device Clock
        Date now = new Date();
        boolean is24 = android.text.format.DateFormat.is24HourFormat(ctx);
        SimpleDateFormat timeSdf = is24
                ? new SimpleDateFormat("HH:mm:ss", Locale.US)
                : new SimpleDateFormat("h:mm:ss a", Locale.US);
        SimpleDateFormat dateSdf = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US);

        binding.tvLiveClockTime.setText(timeSdf.format(now));
        binding.tvLiveClockDate.setText(dateSdf.format(now));

        // 2. Update Countdown Timer with explicit duration units (e.g. 05h 22m 15s)
        if (cachedTargetTime <= 0) {
            binding.tvCountdownTimer.setText("00m 00s");
        } else {
            long remaining = cachedTargetTime - System.currentTimeMillis();
            if (remaining <= 0) {
                binding.tvCountdownTimer.setText("00m 00s");
            } else {
                binding.tvCountdownTimer.setText(TimeUtils.formatCountdownVerbose(remaining));
            }
        }
    }

    private void stopTicker() {
        if (tickerRunnable != null) {
            tickerHandler.removeCallbacks(tickerRunnable);
            tickerRunnable = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTicker();
        binding = null;
    }
}
