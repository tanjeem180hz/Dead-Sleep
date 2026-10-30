package com.deadsleep.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.deadsleep.app.data.DeadSleepRepository;
import com.deadsleep.app.databinding.FragmentScheduleListBinding;
import com.deadsleep.app.manager.ScheduleManager;
import com.deadsleep.app.manager.SleepModeManager;
import com.deadsleep.app.model.SleepSchedule;
import com.deadsleep.app.ui.adapter.ScheduleAdapter;
import com.deadsleep.app.util.Constants;

/**
 * Fragment listing all sleep schedules with toggle, edit, duplicate, and delete capabilities.
 */
public class ScheduleListFragment extends Fragment implements ScheduleAdapter.OnScheduleActionListener {

    private FragmentScheduleListBinding binding;
    private ScheduleAdapter adapter;
    private DeadSleepRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentScheduleListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = DeadSleepRepository.getInstance(requireContext());

        adapter = new ScheduleAdapter(this);
        binding.rvSchedules.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSchedules.setAdapter(adapter);

        repository.getAllSchedulesLiveData().observe(getViewLifecycleOwner(), schedules -> {
            adapter.setSchedules(schedules);
        });

        binding.fabAddSchedule.setOnClickListener(v -> {
            if (!SleepModeManager.getInstance(requireContext()).canEditSchedules()) {
                Toast.makeText(requireContext(), "🔒 Schedule edits are locked during Sleep Mode!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(requireContext(), ScheduleEditActivity.class);
            intent.putExtra(Constants.EXTRA_SCHEDULE_ID, -1L);
            startActivity(intent);
        });

        com.deadsleep.app.util.AnimationHelper.attachBounceFeedback(binding.fabAddSchedule);
    }

    @Override
    public void onToggle(SleepSchedule schedule, boolean isEnabled) {
        if (!SleepModeManager.getInstance(requireContext()).canEditSchedules()) {
            Toast.makeText(requireContext(), "🔒 Schedule edits are locked during Sleep Mode!", Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
            return;
        }
        schedule.setEnabled(isEnabled);
        repository.updateSchedule(schedule);
        ScheduleManager.getInstance(requireContext()).rescheduleAll();
    }

    @Override
    public void onClick(SleepSchedule schedule) {
        if (!SleepModeManager.getInstance(requireContext()).canEditSchedules()) {
            Toast.makeText(requireContext(), "🔒 Schedule edits are locked during Sleep Mode!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(requireContext(), ScheduleEditActivity.class);
        intent.putExtra(Constants.EXTRA_SCHEDULE_ID, schedule.getId());
        startActivity(intent);
    }

    @Override
    public void onDuplicate(SleepSchedule schedule) {
        if (!SleepModeManager.getInstance(requireContext()).canEditSchedules()) {
            Toast.makeText(requireContext(), "🔒 Schedule edits are locked during Sleep Mode!", Toast.LENGTH_SHORT).show();
            return;
        }
        SleepSchedule copy = new SleepSchedule();
        copy.setName(schedule.getName() + " (Copy)");
        copy.setSleepHour(schedule.getSleepHour());
        copy.setSleepMinute(schedule.getSleepMinute());
        copy.setWakeHour(schedule.getWakeHour());
        copy.setWakeMinute(schedule.getWakeMinute());
        copy.setDaysOfWeek(schedule.getDaysOfWeek());
        copy.setEnabled(true);
        copy.setRingtoneUri(schedule.getRingtoneUri());
        copy.setRingtoneTitle(schedule.getRingtoneTitle());
        copy.setVolume(schedule.getVolume());
        copy.setVibrate(schedule.isVibrate());
        copy.setGradualVolume(schedule.isGradualVolume());
        copy.setChallengeType(schedule.getChallengeType());
        copy.setChallengeDifficulty(schedule.getChallengeDifficulty());

        repository.insertSchedule(copy, id -> {
            ScheduleManager.getInstance(requireContext()).rescheduleAll();
        });
    }

    @Override
    public void onDelete(SleepSchedule schedule) {
        if (!SleepModeManager.getInstance(requireContext()).canEditSchedules()) {
            Toast.makeText(requireContext(), "🔒 Schedule edits are locked during Sleep Mode!", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Schedule")
                .setMessage("Are you sure you want to delete \"" + schedule.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    repository.deleteSchedule(schedule);
                    ScheduleManager.getInstance(requireContext()).rescheduleAll();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
