package com.deadsleep.app.ui;

import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.deadsleep.app.data.AppDatabase;
import com.deadsleep.app.data.DeadSleepRepository;
import com.deadsleep.app.data.PreferencesManager;
import com.deadsleep.app.databinding.ActivityBedtimeBinding;
import com.deadsleep.app.manager.BedtimeDisplayManager;
import com.deadsleep.app.manager.ScheduleManager;
import com.deadsleep.app.model.SleepSchedule;
import com.deadsleep.app.util.TimeUtils;

/**
 * Bedtime Routine & Display Settings activity: controls automatic schedule bedtime mode
 * and Black & White (Grayscale) desaturation to eliminate phone dopamine and blue-light eye strain.
 */
public class BedtimeActivity extends AppCompatActivity {

    private ActivityBedtimeBinding binding;
    private PreferencesManager prefs;
    private boolean isTestGrayscaleActive = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBedtimeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = PreferencesManager.getInstance(this);

        initViews();
        loadScheduleSummary();
        updatePermissionStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionStatus();
        loadScheduleSummary();
    }

    private void initViews() {
        binding.btnBack.setOnClickListener(v -> finish());

        // 1. Auto Bedtime Mode Toggle
        binding.switchAutoBedtime.setChecked(prefs.isBedtimeModeAutoEnabled());
        binding.switchAutoBedtime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setBedtimeModeAutoEnabled(isChecked);
            Toast.makeText(this, isChecked ? "Automatic Bedtime Mode Enabled 🌙" : "Automatic Bedtime Mode Disabled", Toast.LENGTH_SHORT).show();
        });

        binding.btnEditSchedule.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("open_tab", "schedule");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                finish();
            }
        });

        // 2. Black & White Grayscale Screen Toggle
        binding.switchGrayscale.setChecked(prefs.isBedtimeGrayscaleEnabled());
        binding.switchGrayscale.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setBedtimeGrayscaleEnabled(isChecked);
            Toast.makeText(this, isChecked ? "B&W Display Mode Enabled 🖤🤍" : "B&W Display Mode Disabled", Toast.LENGTH_SHORT).show();
        });

        // Test Grayscale live
        binding.btnTestGrayscale.setOnClickListener(v -> toggleGrayscaleTest());

        // Open Color Correction Settings
        binding.btnOpenColorCorrection.setOnClickListener(v -> BedtimeDisplayManager.openColorCorrectionSettings(this));

        // 3. ADB Command Copy
        binding.btnCopyAdbCommand.setOnClickListener(v -> {
            try {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("DeadSleep ADB Command", binding.tvAdbCommand.getText());
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "ADB Command Copied to Clipboard! 📋", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Unable to copy command", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Do Not Disturb Toggle
        binding.switchBedtimeDnd.setChecked(prefs.isBedtimeDndEnabled());
        binding.switchBedtimeDnd.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !hasDndPermission()) {
                binding.switchBedtimeDnd.setChecked(false);
                requestDndPermission();
                return;
            }
            prefs.setBedtimeDndEnabled(isChecked);
        });

        binding.btnGrantDndPermission.setOnClickListener(v -> requestDndPermission());
    }

    private void toggleGrayscaleTest() {
        isTestGrayscaleActive = !isTestGrayscaleActive;
        if (isTestGrayscaleActive) {
            BedtimeDisplayManager.applyGrayscaleToView(binding.getRoot());
            BedtimeDisplayManager.enableGrayscale(this);
            binding.btnTestGrayscale.setText("🌈 Restore Vibrant Color");
            Toast.makeText(this, "Black & White (Grayscale) active! 🖤🤍", Toast.LENGTH_SHORT).show();
        } else {
            BedtimeDisplayManager.removeGrayscaleFromView(binding.getRoot());
            BedtimeDisplayManager.disableGrayscale(this);
            binding.btnTestGrayscale.setText("👁️ Test Black & White Screen Now");
            Toast.makeText(this, "Vibrant full color restored! 🌈", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadScheduleSummary() {
        AppDatabase.getExecutor().execute(() -> {
            try {
                long currentSchedId = prefs.getCurrentScheduleId();
                SleepSchedule schedule = DeadSleepRepository.getInstance(this).getScheduleById(currentSchedId);
                if (schedule == null) {
                    schedule = ScheduleManager.getInstance(this).getNextActiveSchedule();
                }

                final SleepSchedule finalSched = schedule;
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed() || binding == null) return;
                    if (finalSched != null) {
                        String bedtime = TimeUtils.formatTime(finalSched.getBedtimeHour(), finalSched.getBedtimeMinute());
                        String wake = TimeUtils.formatTime(finalSched.getWakeHour(), finalSched.getWakeMinute());
                        long durationMin = TimeUtils.calculateDurationMinutes(
                                finalSched.getBedtimeHour(), finalSched.getBedtimeMinute(),
                                finalSched.getWakeHour(), finalSched.getWakeMinute()
                        );
                        long durHrs = durationMin / 60;
                        long durMins = durationMin % 60;
                        binding.tvCurrentScheduleInfo.setText(
                                String.format("Current Schedule: %s → %s (%dh %02dm)", bedtime, wake, durHrs, durMins)
                        );
                    } else {
                        binding.tvCurrentScheduleInfo.setText("Current Schedule: 11:00 PM → 07:00 AM (8h 00m)");
                    }
                });
            } catch (Exception ignored) {}
        });
    }

    private void updatePermissionStatus() {
        boolean hasWriteSecure = BedtimeDisplayManager.hasWriteSecurePermission(this);
        if (hasWriteSecure) {
            binding.tvAdbPermissionStatus.setText("✅ Status: System-Wide Monochromacy Granted (Automatic)");
            binding.tvAdbPermissionStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            binding.tvAdbPermissionStatus.setText("⚠️ Status: In-App Grayscale Active (Run ADB for System-Wide)");
            binding.tvAdbPermissionStatus.setTextColor(Color.parseColor("#FFB74D"));
        }

        if (hasDndPermission()) {
            binding.btnGrantDndPermission.setVisibility(View.GONE);
        } else {
            binding.btnGrantDndPermission.setVisibility(View.VISIBLE);
        }
    }

    private boolean hasDndPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            return nm != null && nm.isNotificationPolicyAccessGranted();
        }
        return true;
    }

    private void requestDndPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Could not open DND settings", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isTestGrayscaleActive) {
            BedtimeDisplayManager.removeGrayscaleFromView(binding.getRoot());
            BedtimeDisplayManager.disableGrayscale(this);
        }
    }
}
