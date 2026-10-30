package com.deadsleep.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.deadsleep.app.data.AppDatabase;
import com.deadsleep.app.databinding.FragmentStatsBinding;
import com.deadsleep.app.manager.StatsManager;

/**
 * Screen visualizing sleep consistency score, weekly duration chart, and streak achievements.
 */
public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;
    private StatsManager statsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        statsManager = StatsManager.getInstance(requireContext());

        com.deadsleep.app.util.AnimationHelper.attachBounceFeedback(binding.badge3Day);
        com.deadsleep.app.util.AnimationHelper.attachBounceFeedback(binding.badge7Day);
        com.deadsleep.app.util.AnimationHelper.attachBounceFeedback(binding.badge30Day);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics();
    }

    private void loadStatistics() {
        AppDatabase.getExecutor().execute(() -> {
            int consistency = statsManager.calculateConsistencyScore();
            StatsManager.ChartDataPoint[] chartPoints = statsManager.getWeeklySleepDurationData();
            boolean has3 = statsManager.hasBadge3Day();
            boolean has7 = statsManager.hasBadge7Day();
            boolean has30 = statsManager.hasBadge30Day();

            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.gaugeConsistency.setScore(consistency);
                    binding.chartSleepDuration.setData(chartPoints);

                    // Badge alpha states
                    binding.badge3Day.setAlpha(has3 ? 1.0f : 0.35f);
                    binding.badge7Day.setAlpha(has7 ? 1.0f : 0.35f);
                    binding.badge30Day.setAlpha(has30 ? 1.0f : 0.35f);
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
