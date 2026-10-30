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
    protected void onResume() {
        super.onResume();
        // Mandatory update check on resuming dashboard
        com.deadsleep.app.manager.UpdateManager.getInstance(this).checkForUpdate((updateAvailable, info, error) -> {
            if (isFinishing() || isDestroyed()) return;
            if (updateAvailable && info != null) {
                android.content.Intent updateIntent = new android.content.Intent(this, ForceUpdateActivity.class);
                updateIntent.putExtra(ForceUpdateActivity.EXTRA_UPDATE_INFO, info);
                startActivity(updateIntent);
            }
        });
    }

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
                    .commitAllowingStateLoss();
        }

        handleOpenTab(getIntent());

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
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.navHostFragment, selected)
                        .commitAllowingStateLoss();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleOpenTab(intent);
    }

    private void handleOpenTab(android.content.Intent intent) {
        if (intent == null || binding == null) return;
        String openTab = intent.getStringExtra("open_tab");
        if ("schedule".equals(openTab)) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_schedule);
        } else if ("apps".equals(openTab)) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_apps);
        } else if ("stats".equals(openTab)) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_stats);
        } else if ("settings".equals(openTab)) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_settings);
        }
    }
}
