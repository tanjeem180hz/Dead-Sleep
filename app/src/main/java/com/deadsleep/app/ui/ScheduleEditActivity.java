package com.deadsleep.app.ui;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.deadsleep.app.challenge.ChallengeFactory;
import com.deadsleep.app.data.AppDatabase;
import com.deadsleep.app.data.DeadSleepRepository;
import com.deadsleep.app.databinding.ActivityScheduleEditBinding;
import com.deadsleep.app.manager.ScheduleManager;
import com.deadsleep.app.model.SleepSchedule;
import com.deadsleep.app.util.Constants;
import com.deadsleep.app.util.SoundPlayer;
import com.deadsleep.app.util.TimeUtils;

/**
 * Full editor for creating and modifying sleep schedules with midnight math and duration alerts.
 */
public class ScheduleEditActivity extends AppCompatActivity {

    private ActivityScheduleEditBinding binding;
    private DeadSleepRepository repository;
    private SoundPlayer soundPlayer;

    private long scheduleId = -1;
    private SleepSchedule currentSchedule;

    private int sleepHour = 23;
    private int sleepMinute = 0;
    private int wakeHour = 7;
    private int wakeMinute = 0;

    private static final String[] RINGTONE_NAMES = {
            "Gentle Chime", "Morning Dawn", "Digital Beep", "Cosmic Echo", "Wake Bell"
    };

    private static final String[] RINGTONE_URIS = {
            "bundled:tone_gentle_chime",
            "bundled:tone_morning_dawn",
            "bundled:tone_digital_beep",
            "bundled:tone_cosmic_echo",
            "bundled:tone_wake_bell"
    };

    private static final String[] DIFFICULTIES = {
            Constants.DIFFICULTY_EASY,
            Constants.DIFFICULTY_MEDIUM,
            Constants.DIFFICULTY_HARD
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = DeadSleepRepository.getInstance(this);
        soundPlayer = new SoundPlayer(this);
        scheduleId = getIntent().getLongExtra(Constants.EXTRA_SCHEDULE_ID, -1L);

        setupSpinners();
        setupListeners();

        if (scheduleId != -1) {
            binding.tvEditorTitle.setText("Edit Schedule");
            AppDatabase.getExecutor().execute(() -> {
                currentSchedule = repository.getScheduleById(scheduleId);
                if (currentSchedule != null) {
                    runOnUiThread(this::populateUI);
                }
            });
        } else {
            binding.tvEditorTitle.setText("New Schedule");
            currentSchedule = new SleepSchedule();
            populateUI();
        }
    }

    private void setupSpinners() {
        // Ringtone Spinner
        ArrayAdapter<String> ringtoneAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, RINGTONE_NAMES
        );
        binding.spinnerRingtone.setAdapter(ringtoneAdapter);

        // Challenge Types Spinner
        String[] challengeTypes = ChallengeFactory.getAvailableChallengeTypes(this);
        ArrayAdapter<String> challengeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, challengeTypes
        );
        binding.spinnerChallengeType.setAdapter(challengeAdapter);

        // Difficulty Spinner
        ArrayAdapter<String> diffAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, DIFFICULTIES
        );
        binding.spinnerDifficulty.setAdapter(diffAdapter);
    }

    private void populateUI() {
        if (currentSchedule == null) return;

        binding.etScheduleName.setText(currentSchedule.getName());
        sleepHour = currentSchedule.getSleepHour();
        sleepMinute = currentSchedule.getSleepMinute();
        wakeHour = currentSchedule.getWakeHour();
        wakeMinute = currentSchedule.getWakeMinute();

        binding.tvSleepTime.setText(TimeUtils.formatTime(sleepHour, sleepMinute));
        binding.tvWakeTime.setText(TimeUtils.formatTime(wakeHour, wakeMinute));
        updateDurationWarning();

        // Populate days
        int mask = currentSchedule.getDaysOfWeek();
        binding.chipMon.setChecked((mask & TimeUtils.DAY_MON) != 0);
        binding.chipTue.setChecked((mask & TimeUtils.DAY_TUE) != 0);
        binding.chipWed.setChecked((mask & TimeUtils.DAY_WED) != 0);
        binding.chipThu.setChecked((mask & TimeUtils.DAY_THU) != 0);
        binding.chipFri.setChecked((mask & TimeUtils.DAY_FRI) != 0);
        binding.chipSat.setChecked((mask & TimeUtils.DAY_SAT) != 0);
        binding.chipSun.setChecked((mask & TimeUtils.DAY_SUN) != 0);

        // Audio & vibration
        binding.seekVolume.setProgress(currentSchedule.getVolume());
        binding.cbVibrate.setChecked(currentSchedule.isVibrate());
        binding.cbGradualVolume.setChecked(currentSchedule.isGradualVolume());
        binding.cbAutoEscalate.setChecked(currentSchedule.isAutoEscalateChallenge());

        for (int i = 0; i < RINGTONE_URIS.length; i++) {
            if (RINGTONE_URIS[i].equals(currentSchedule.getRingtoneUri())) {
                binding.spinnerRingtone.setSelection(i);
                break;
            }
        }

        // Challenge
        String[] types = ChallengeFactory.getAvailableChallengeTypes(this);
        for (int i = 0; i < types.length; i++) {
            if (types[i].equalsIgnoreCase(currentSchedule.getChallengeType())) {
                binding.spinnerChallengeType.setSelection(i);
                break;
            }
        }

        for (int i = 0; i < DIFFICULTIES.length; i++) {
            if (DIFFICULTIES[i].equalsIgnoreCase(currentSchedule.getChallengeDifficulty())) {
                binding.spinnerDifficulty.setSelection(i);
                break;
            }
        }
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.cardSleepTime.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                sleepHour = hourOfDay;
                sleepMinute = minute;
                binding.tvSleepTime.setText(TimeUtils.formatTime(sleepHour, sleepMinute));
                updateDurationWarning();
            }, sleepHour, sleepMinute, false);
            dialog.show();
        });

        binding.cardWakeTime.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                wakeHour = hourOfDay;
                wakeMinute = minute;
                binding.tvWakeTime.setText(TimeUtils.formatTime(wakeHour, wakeMinute));
                updateDurationWarning();
            }, wakeHour, wakeMinute, false);
            dialog.show();
        });

        binding.btnPreviewSound.setOnClickListener(v -> {
            int selectedIdx = binding.spinnerRingtone.getSelectedItemPosition();
            String uri = (selectedIdx >= 0 && selectedIdx < RINGTONE_URIS.length) ? RINGTONE_URIS[selectedIdx] : RINGTONE_URIS[0];
            int vol = binding.seekVolume.getProgress();
            soundPlayer.playPreview(uri, vol);
        });

        binding.btnSaveSchedule.setOnClickListener(v -> saveSchedule());
    }

    private void updateDurationWarning() {
        int durationMinutes = TimeUtils.calculateDurationMinutes(sleepHour, sleepMinute, wakeHour, wakeMinute);
        binding.tvDurationDisplay.setText("Sleep Duration: " + TimeUtils.formatDuration(durationMinutes));

        if (durationMinutes < 360) { // < 6 hours
            binding.tvDurationWarning.setText("⚠️ Warning: Less than 6 hours of sleep can lead to chronic fatigue.");
            binding.tvDurationWarning.setVisibility(View.VISIBLE);
        } else if (durationMinutes > 600) { // > 10 hours
            binding.tvDurationWarning.setText("ℹ️ Note: Over 10 hours may cause grogginess.");
            binding.tvDurationWarning.setVisibility(View.VISIBLE);
        } else {
            binding.tvDurationWarning.setVisibility(View.GONE);
        }
    }

    private void saveSchedule() {
        String name = binding.etScheduleName.getText() != null ? binding.etScheduleName.getText().toString().trim() : "";
        if (name.isEmpty()) name = "Bedtime Routine";

        int daysMask = 0;
        if (binding.chipMon.isChecked()) daysMask |= TimeUtils.DAY_MON;
        if (binding.chipTue.isChecked()) daysMask |= TimeUtils.DAY_TUE;
        if (binding.chipWed.isChecked()) daysMask |= TimeUtils.DAY_WED;
        if (binding.chipThu.isChecked()) daysMask |= TimeUtils.DAY_THU;
        if (binding.chipFri.isChecked()) daysMask |= TimeUtils.DAY_FRI;
        if (binding.chipSat.isChecked()) daysMask |= TimeUtils.DAY_SAT;
        if (binding.chipSun.isChecked()) daysMask |= TimeUtils.DAY_SUN;

        if (daysMask == 0) {
            Toast.makeText(this, "Please select at least one repeat day", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentSchedule == null) currentSchedule = new SleepSchedule();
        currentSchedule.setName(name);
        currentSchedule.setSleepHour(sleepHour);
        currentSchedule.setSleepMinute(sleepMinute);
        currentSchedule.setWakeHour(wakeHour);
        currentSchedule.setWakeMinute(wakeMinute);
        currentSchedule.setDaysOfWeek(daysMask);

        int ringtoneIdx = binding.spinnerRingtone.getSelectedItemPosition();
        currentSchedule.setRingtoneUri(RINGTONE_URIS[Math.max(0, ringtoneIdx)]);
        currentSchedule.setRingtoneTitle(RINGTONE_NAMES[Math.max(0, ringtoneIdx)]);

        currentSchedule.setVolume(binding.seekVolume.getProgress());
        currentSchedule.setVibrate(binding.cbVibrate.isChecked());
        currentSchedule.setGradualVolume(binding.cbGradualVolume.isChecked());
        currentSchedule.setAutoEscalateChallenge(binding.cbAutoEscalate.isChecked());

        Object selChallenge = binding.spinnerChallengeType.getSelectedItem();
        currentSchedule.setChallengeType(selChallenge != null ? selChallenge.toString() : Constants.CHALLENGE_RANDOM);

        Object selDiff = binding.spinnerDifficulty.getSelectedItem();
        currentSchedule.setChallengeDifficulty(selDiff != null ? selDiff.toString() : Constants.DIFFICULTY_MEDIUM);

        if (scheduleId == -1) {
            repository.insertSchedule(currentSchedule, id -> {
                ScheduleManager.getInstance(this).rescheduleAll();
                finish();
            });
        } else {
            repository.updateSchedule(currentSchedule);
            ScheduleManager.getInstance(this).rescheduleAll();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPlayer != null) {
            soundPlayer.stop();
        }
    }
}
