package com.deadsleep.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deadsleep.app.R;
import com.deadsleep.app.model.WhitelistEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for managing allowed whitelist apps and contacts.
 */
public class WhitelistAdapter extends RecyclerView.Adapter<WhitelistAdapter.WhitelistViewHolder> {

    public interface OnWhitelistRemoveListener {
        void onRemove(WhitelistEntry entry);
    }

    private List<WhitelistEntry> entries = new ArrayList<>();
    private final OnWhitelistRemoveListener listener;

    public WhitelistAdapter(OnWhitelistRemoveListener listener) {
        this.listener = listener;
    }

    public void setEntries(List<WhitelistEntry> list) {
        this.entries = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WhitelistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_whitelist, parent, false);
        return new WhitelistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WhitelistViewHolder holder, int position) {
        WhitelistEntry entry = entries.get(position);

        holder.tvName.setText(entry.getDisplayName());
        holder.tvIdentifier.setText(entry.getIdentifier());
        holder.tvType.setText(entry.getType());

        if (entry.isEmergency()) {
            holder.btnRemove.setVisibility(View.GONE);
            holder.tvEmergency.setVisibility(View.VISIBLE);
        } else {
            holder.btnRemove.setVisibility(View.VISIBLE);
            holder.tvEmergency.setVisibility(View.GONE);
            holder.btnRemove.setOnClickListener(v -> {
                if (listener != null) listener.onRemove(entry);
            });
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class WhitelistViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvIdentifier, tvType, tvEmergency;
        ImageButton btnRemove;

        WhitelistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvWhitelistName);
            tvIdentifier = itemView.findViewById(R.id.tvWhitelistIdentifier);
            tvType = itemView.findViewById(R.id.tvWhitelistType);
            tvEmergency = itemView.findViewById(R.id.tvWhitelistEmergency);
            btnRemove = itemView.findViewById(R.id.btnRemoveWhitelist);
        }
    }
}
