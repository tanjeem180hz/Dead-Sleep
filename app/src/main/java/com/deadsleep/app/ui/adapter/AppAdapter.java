package com.deadsleep.app.ui.adapter;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deadsleep.app.R;
import com.deadsleep.app.model.RestrictedApp;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for browsing, searching, and toggling restricted applications.
 */
public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

    public interface OnAppToggleListener {
        void onToggle(RestrictedApp app, boolean isRestricted);
    }

    private List<RestrictedApp> apps = new ArrayList<>();
    private final PackageManager packageManager;
    private final OnAppToggleListener listener;

    public AppAdapter(PackageManager pm, OnAppToggleListener listener) {
        this.packageManager = pm;
        this.listener = listener;
    }

    public void setApps(List<RestrictedApp> list) {
        this.apps = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        RestrictedApp app = apps.get(position);

        holder.tvName.setText(app.getAppName());
        holder.tvPackage.setText(app.getPackageName());
        holder.tvCategory.setText(app.getCategory());

        if (app.getBlockCount() > 0) {
            holder.tvBlockCount.setVisibility(View.VISIBLE);
            holder.tvBlockCount.setText(app.getBlockCount() + " blocked");
        } else {
            holder.tvBlockCount.setVisibility(View.GONE);
        }

        try {
            if (packageManager != null) {
                Drawable icon = packageManager.getApplicationIcon(app.getPackageName());
                holder.ivIcon.setImageDrawable(icon);
            } else {
                holder.ivIcon.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } catch (Exception e) {
            holder.ivIcon.setImageResource(R.drawable.ic_launcher_foreground);
        }

        holder.cbRestricted.setOnCheckedChangeListener(null);
        holder.cbRestricted.setChecked(app.isRestricted());
        holder.cbRestricted.setOnCheckedChangeListener((btn, isChecked) -> {
            app.setRestricted(isChecked);
            if (listener != null) listener.onToggle(app, isChecked);
        });

        holder.itemView.setOnClickListener(v -> holder.cbRestricted.performClick());
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName, tvPackage, tvCategory, tvBlockCount;
        CheckBox cbRestricted;

        AppViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivAppIcon);
            tvName = itemView.findViewById(R.id.tvAppName);
            tvPackage = itemView.findViewById(R.id.tvAppPackage);
            tvCategory = itemView.findViewById(R.id.tvAppCategory);
            tvBlockCount = itemView.findViewById(R.id.tvAppBlockCount);
            cbRestricted = itemView.findViewById(R.id.cbAppRestricted);
        }
    }
}
