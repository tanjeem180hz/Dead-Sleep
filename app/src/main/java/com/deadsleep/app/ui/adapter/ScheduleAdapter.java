package com.deadsleep.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.deadsleep.app.R;
import com.deadsleep.app.model.SleepSchedule;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for rendering user sleep schedules with toggle, edit, and duplicate actions.
 */
public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    public interface OnScheduleActionListener {
        void onToggle(SleepSchedule schedule, boolean isEnabled);
        void onClick(SleepSchedule schedule);
        void onDuplicate(SleepSchedule schedule);
        void onDelete(SleepSchedule schedule);
    }

    private List<SleepSchedule> schedules = new ArrayList<>();
    private final OnScheduleActionListener listener;

    public ScheduleAdapter(OnScheduleActionListener listener) {
        this.listener = listener;
    }

    public void setSchedules(List<SleepSchedule> list) {
        this.schedules = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        SleepSchedule schedule = schedules.get(position);

        android.content.Context ctx = holder.itemView.getContext();
        holder.tvName.setText(schedule.getName());
        holder.tvTimes.setText(schedule.getFormattedSleepTime(ctx) + " → " + schedule.getFormattedWakeTime(ctx));
        holder.tvDays.setText(schedule.getFormattedDays());
        holder.tvDuration.setText(schedule.getFormattedDuration());
        holder.tvChallenge.setText(schedule.getChallengeType() + " (" + schedule.getChallengeDifficulty() + ")");

        holder.switchEnabled.setOnCheckedChangeListener(null);
        holder.switchEnabled.setChecked(schedule.isEnabled());
        holder.switchEnabled.setOnCheckedChangeListener((btn, isChecked) -> {
            if (listener != null) listener.onToggle(schedule, isChecked);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(schedule);
        });

        holder.btnDuplicate.setOnClickListener(v -> {
            if (listener != null) listener.onDuplicate(schedule);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(schedule);
        });
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTimes, tvDays, tvDuration, tvChallenge;
        SwitchCompat switchEnabled;
        ImageButton btnDuplicate, btnDelete;

        ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvScheduleName);
            tvTimes = itemView.findViewById(R.id.tvScheduleTimes);
            tvDays = itemView.findViewById(R.id.tvScheduleDays);
            tvDuration = itemView.findViewById(R.id.tvScheduleDuration);
            tvChallenge = itemView.findViewById(R.id.tvScheduleChallenge);
            switchEnabled = itemView.findViewById(R.id.switchScheduleEnabled);
            btnDuplicate = itemView.findViewById(R.id.btnDuplicateSchedule);
            btnDelete = itemView.findViewById(R.id.btnDeleteSchedule);
        }
    }
}
