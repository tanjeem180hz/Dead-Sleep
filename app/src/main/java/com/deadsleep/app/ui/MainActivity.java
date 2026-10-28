package com.deadsleep.app.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.deadsleep.app.R;
import com.deadsleep.app.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;

/**
 * Main dashboard container hosting the five primary tabs:
 * Home, Schedules, Restricted Apps, Statistics, and Settings.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Fragment homeFragment;
    private Fragment scheduleFragment;
    private Fragment appsFragment;
    private Fragment statsFragment;
    private Fragment settingsFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        homeFragment = new HomeFragment();
        scheduleFragment = new ScheduleListFragment();
        appsFragment = new AppsFragment();
        statsFragment = new StatsFragment();
        settingsFragment = new SettingsFragment();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.navHostFragment, homeFragment)
                    .commit();
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selected = homeFragment;
            } else if (itemId == R.id.nav_schedule) {
                selected = scheduleFragment;
            } else if (itemId == R.id.nav_apps) {
                selected = appsFragment;
            } else if (itemId == R.id.nav_stats) {
                selected = statsFragment;
            } else if (itemId == R.id.nav_settings) {
                selected = settingsFragment;
            }

            if (selected != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.navHostFragment, selected)
                        .commit();
                return true;
            }
            return false;
        });
    }
}
