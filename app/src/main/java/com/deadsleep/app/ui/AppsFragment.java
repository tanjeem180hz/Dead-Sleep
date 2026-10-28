package com.deadsleep.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.deadsleep.app.R;
import com.deadsleep.app.data.DeadSleepRepository;
import com.deadsleep.app.databinding.FragmentAppsBinding;
import com.deadsleep.app.model.RestrictedApp;
import com.deadsleep.app.ui.adapter.AppAdapter;
import com.deadsleep.app.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen for browsing, searching, and toggling restricted apps by category.
 */
public class AppsFragment extends Fragment implements AppAdapter.OnAppToggleListener {

    private FragmentAppsBinding binding;
    private AppAdapter adapter;
    private DeadSleepRepository repository;
    private List<RestrictedApp> allAppsList = new ArrayList<>();
    private String currentCategory = "All";
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAppsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = DeadSleepRepository.getInstance(requireContext());

        adapter = new AppAdapter(requireContext().getPackageManager(), this);
        binding.rvApps.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvApps.setAdapter(adapter);

        repository.getAllAppsLiveData().observe(getViewLifecycleOwner(), apps -> {
            allAppsList = apps != null ? apps : new ArrayList<>();
            applyFilter();
        });

        binding.etSearchApps.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                currentQuery = s != null ? s.toString().trim().toLowerCase() : "";
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipCatSocial) currentCategory = Constants.CATEGORY_SOCIAL;
            else if (id == R.id.chipCatGames) currentCategory = Constants.CATEGORY_GAMES;
            else if (id == R.id.chipCatVideo) currentCategory = Constants.CATEGORY_VIDEO;
            else if (id == R.id.chipCatBrowsers) currentCategory = Constants.CATEGORY_BROWSERS;
            else currentCategory = "All";
            applyFilter();
        });

        binding.btnSelectAllCategory.setOnClickListener(v -> {
            boolean shouldSelect = true;
            for (RestrictedApp app : getFilteredApps()) {
                if (app.isRestricted()) {
                    shouldSelect = false;
                    break;
                }
            }
            for (RestrictedApp app : getFilteredApps()) {
                app.setRestricted(shouldSelect);
                repository.insertOrUpdateApp(app);
            }
            applyFilter();
        });

        binding.btnWhitelist.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), WhitelistActivity.class));
        });
    }

    private List<RestrictedApp> getFilteredApps() {
        List<RestrictedApp> filtered = new ArrayList<>();
        for (RestrictedApp app : allAppsList) {
            boolean matchesCat = "All".equals(currentCategory) || currentCategory.equalsIgnoreCase(app.getCategory());
            boolean matchesQuery = currentQuery.isEmpty()
                    || app.getAppName().toLowerCase().contains(currentQuery)
                    || app.getPackageName().toLowerCase().contains(currentQuery);

            if (matchesCat && matchesQuery) {
                filtered.add(app);
            }
        }
        return filtered;
    }

    private void applyFilter() {
        List<RestrictedApp> filtered = getFilteredApps();
        adapter.setApps(filtered);

        int restrictedCount = 0;
        for (RestrictedApp a : allAppsList) {
            if (a.isRestricted()) restrictedCount++;
        }
        binding.tvSelectedCount.setText(restrictedCount + " Apps Restricted");
    }

    @Override
    public void onToggle(RestrictedApp app, boolean isRestricted) {
        app.setRestricted(isRestricted);
        repository.insertOrUpdateApp(app);
        applyFilter();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
