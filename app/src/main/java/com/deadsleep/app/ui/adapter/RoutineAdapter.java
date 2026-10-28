package com.deadsleep.app.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deadsleep.app.R;
import com.deadsleep.app.model.RoutineSuggestion;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for rendering intelligent routine advisor recommendations.
 */
public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder> {

    public interface OnApplySuggestionListener {
        void onApply(RoutineSuggestion suggestion);
    }

    private List<RoutineSuggestion> suggestions = new ArrayList<>();
    private final OnApplySuggestionListener listener;

    public RoutineAdapter(OnApplySuggestionListener listener) {
        this.listener = listener;
    }

    public void setSuggestions(List<RoutineSuggestion> list) {
        this.suggestions = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_routine_suggestion, parent, false);
        return new RoutineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {
        RoutineSuggestion item = suggestions.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvDescription.setText(item.getDescription());
        holder.btnApply.setText(item.getActionLabel());

        if (item.isApplied()) {
            holder.btnApply.setEnabled(false);
            holder.btnApply.setText("✓ Applied");
            holder.btnApply.setBackgroundColor(Color.parseColor("#1E382B"));
        } else {
            holder.btnApply.setEnabled(true);
            holder.btnApply.setBackgroundColor(Color.parseColor("#35248A"));
            holder.btnApply.setOnClickListener(v -> {
                if (listener != null) listener.onApply(item);
                notifyItemChanged(position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    static class RoutineViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription;
        Button btnApply;

        RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRoutineTitle);
            tvDescription = itemView.findViewById(R.id.tvRoutineDescription);
            btnApply = itemView.findViewById(R.id.btnApplyRoutine);
        }
    }
}
