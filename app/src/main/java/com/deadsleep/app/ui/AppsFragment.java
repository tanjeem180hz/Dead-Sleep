package com.deadsleep.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.deadsleep.app.R;
import com.deadsleep.app.data.DeadSleepRepository;
import com.deadsleep.app.databinding.FragmentAppsBinding;
import com.deadsleep.app.manager.RestrictionManager;
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

        // Immediate background scan of device applications
        RestrictionManager.getInstance(requireContext()).scanAndSeedInstalledApps();

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
            else if (id == R.id.chipCatOther) currentCategory = Constants.CATEGORY_OTHER;
            else currentCategory = "All";
            applyFilter();
        });

        binding.btnSelectAllCategory.setOnClickListener(v -> {
            List<RestrictedApp> filtered = getFilteredApps();
            if (filtered.isEmpty()) return;

            boolean shouldSelect = true;
            for (RestrictedApp app : filtered) {
                if (app.isRestricted()) {
                    shouldSelect = false;
                    break;
                }
            }
            for (RestrictedApp app : filtered) {
                app.setRestricted(shouldSelect);
                repository.insertOrUpdateApp(app);
            }
            applyFilter();
        });

        binding.btnRescanApps.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Scanning device for installed apps...", Toast.LENGTH_SHORT).show();
            RestrictionManager.getInstance(requireContext()).scanAndSeedInstalledApps();
        });

        binding.btnWhitelist.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), WhitelistActivity.class));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            RestrictionManager.getInstance(requireContext()).scanAndSeedInstalledApps();
        }
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
        if (binding == null) return;
        List<RestrictedApp> filtered = getFilteredApps();
        adapter.setApps(filtered);

        if (filtered.isEmpty()) {
            binding.rvApps.setVisibility(View.GONE);
            binding.tvEmptyApps.setVisibility(View.VISIBLE);
            if (!currentQuery.isEmpty()) {
                binding.tvEmptyApps.setText("No apps found matching \"" + currentQuery + "\"");
            } else if (!"All".equals(currentCategory)) {
                binding.tvEmptyApps.setText("No apps found in " + currentCategory + " category.");
            } else {
                binding.tvEmptyApps.setText("Scanning installed apps on device...");
            }
        } else {
            binding.rvApps.setVisibility(View.VISIBLE);
            binding.tvEmptyApps.setVisibility(View.GONE);
        }

        int restrictedCount = 0;
        for (RestrictedApp a : allAppsList) {
            if (a.isRestricted()) restrictedCount++;
        }

        if (allAppsList.isEmpty()) {
            binding.tvSelectedCount.setText("Detecting installed apps...");
        } else {
            binding.tvSelectedCount.setText(restrictedCount + " of " + allAppsList.size() + " Apps Restricted");
        }
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
