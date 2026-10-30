package com.deadsleep.app.ui;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.deadsleep.app.challenge.ChallengeFactory;
import com.deadsleep.app.data.PreferencesManager;
import com.deadsleep.app.databinding.FragmentSettingsBinding;
import com.deadsleep.app.manager.ScheduleManager;
import com.deadsleep.app.manager.SettingsManager;
import com.deadsleep.app.manager.SleepModeManager;
import com.deadsleep.app.receiver.DeadSleepDeviceAdminReceiver;
import com.deadsleep.app.util.Constants;

import java.io.File;

/**
 * Settings tab handling nudges, alarm tests, challenge practice, tamper protection, and backup/export.
 */
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private PreferencesManager prefs;
    private SettingsManager settingsManager;

    private static final Integer[] NUDGE_MINUTES = {15, 30, 45, 60};
    private static final String[] NUDGE_LABELS = {
            "15 minutes before", "30 minutes before (Default)", "45 minutes before", "60 minutes before"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = PreferencesManager.getInstance(requireContext());
        settingsManager = SettingsManager.getInstance(requireContext());

        setupNudgeSettings();
        setupActionButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && prefs != null) {
            binding.switchSettingsAutoBedtime.setChecked(prefs.isBedtimeModeAutoEnabled());
            binding.switchSettingsGrayscale.setChecked(prefs.isBedtimeGrayscaleEnabled());
        }
    }

    private void setupNudgeSettings() {
        ArrayAdapter<String> nudgeAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, NUDGE_LABELS
        );
        binding.spinnerNudgeLead.setAdapter(nudgeAdapter);

        int currentLead = prefs.getNudgeLeadMinutes();
        for (int i = 0; i < NUDGE_MINUTES.length; i++) {
            if (NUDGE_MINUTES[i] == currentLead) {
                binding.spinnerNudgeLead.setSelection(i);
                break;
            }
        }

        binding.spinnerNudgeLead.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.setNudgeLeadMinutes(NUDGE_MINUTES[position]);
                ScheduleManager.getInstance(requireContext()).rescheduleAll();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.switchSecondNudge.setChecked(prefs.isSecondNudgeEnabled());
        binding.switchSecondNudge.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.setSecondNudgeEnabled(isChecked);
            ScheduleManager.getInstance(requireContext()).rescheduleAll();
        });
    }

    private void setupActionButtons() {
        binding.btnOpenBedtimeSectionSettings.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), BedtimeActivity.class));
        });

        binding.switchSettingsAutoBedtime.setChecked(prefs.isBedtimeModeAutoEnabled());
        binding.switchSettingsAutoBedtime.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.setBedtimeModeAutoEnabled(isChecked);
            Toast.makeText(requireContext(), isChecked ? "Auto Bedtime Mode Enabled 🌙" : "Auto Bedtime Mode Disabled", Toast.LENGTH_SHORT).show();
        });

        binding.switchSettingsGrayscale.setChecked(prefs.isBedtimeGrayscaleEnabled());
        binding.switchSettingsGrayscale.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.setBedtimeGrayscaleEnabled(isChecked);
            Toast.makeText(requireContext(), isChecked ? "B&W Display Mode Enabled 🖤🤍" : "B&W Display Mode Disabled", Toast.LENGTH_SHORT).show();
        });

        binding.btnTestAlarm.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AlarmTestActivity.class));
        });

        binding.btnPracticeChallenge.setOnClickListener(v -> showPracticeChallengePicker());

        binding.btnPermissionsHealth.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), PermissionWizardActivity.class));
        });

        binding.btnWhitelistSettings.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), WhitelistActivity.class));
        });

        binding.btnDisableProtection.setOnClickListener(v -> {
            if (SleepModeManager.getInstance(requireContext()).isSleepModeActive()) {
                Toast.makeText(requireContext(), "🛡️ Tamper Protection cannot be disabled during Sleep Mode!", Toast.LENGTH_LONG).show();
                return;
            }

            DevicePolicyManager dpm = (DevicePolicyManager) requireContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName admin = new ComponentName(requireContext(), DeadSleepDeviceAdminReceiver.class);
            if (dpm != null && dpm.isAdminActive(admin)) {
                dpm.removeActiveAdmin(admin);
                Toast.makeText(requireContext(), "Device Admin removed successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Device Admin is not currently active.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnExportCSV.setOnClickListener(v -> {
            try {
                File f = settingsManager.exportStatisticsCSV();
                Toast.makeText(requireContext(), "Exported to: " + f.getName(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnExportJSON.setOnClickListener(v -> {
            try {
                File f = settingsManager.exportBackupJSON();
                Toast.makeText(requireContext(), "Backup saved: " + f.getName(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Backup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnResetData.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Reset All Data")
                    .setMessage("This will erase all schedules, statistics, and preferences. Are you sure?")
                    .setPositiveButton("Reset", (dialog, which) -> {
                        settingsManager.resetAllData();
                        Toast.makeText(requireContext(), "All data reset.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        binding.btnCheckUpdates.setText("🚀 Check for Updates (v" + com.deadsleep.app.BuildConfig.VERSION_NAME + ")");
        binding.btnCheckUpdates.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Checking GitHub for updates...", Toast.LENGTH_SHORT).show();
            com.deadsleep.app.manager.UpdateManager.getInstance(requireContext()).checkForUpdate((updateAvailable, info, error) -> {
                if (!isAdded()) return;
                if (updateAvailable && info != null) {
                    Intent updateIntent = new Intent(requireContext(), ForceUpdateActivity.class);
                    updateIntent.putExtra(ForceUpdateActivity.EXTRA_UPDATE_INFO, info);
                    startActivity(updateIntent);
                } else if (error != null) {
                    Toast.makeText(requireContext(), "Update check: " + error, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "You are on the latest version! (v" + com.deadsleep.app.BuildConfig.VERSION_NAME + ") ✨", Toast.LENGTH_LONG).show();
                }
            });
        });

        binding.btnAbout.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AboutActivity.class));
        });
    }

    private void showPracticeChallengePicker() {
        String[] types = ChallengeFactory.getAvailableChallengeTypes(requireContext());
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Challenge to Practice")
                .setItems(types, (dialog, which) -> {
                    String chosenType = types[which];
                    Intent intent = new Intent(requireContext(), ChallengeActivity.class);
                    intent.putExtra(Constants.EXTRA_IS_PRACTICE, true);
                    intent.putExtra(Constants.EXTRA_CHALLENGE_TYPE, chosenType);
                    intent.putExtra(Constants.EXTRA_CHALLENGE_DIFFICULTY, Constants.DIFFICULTY_MEDIUM);
                    startActivity(intent);
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
