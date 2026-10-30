package com.deadsleep.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.deadsleep.app.R;
import com.deadsleep.app.data.PreferencesManager;
import com.deadsleep.app.manager.RestrictionManager;
import com.deadsleep.app.manager.ScheduleManager;
import com.deadsleep.app.manager.UpdateManager;

/**
 * Splash entry screen routing user to Onboarding, Active Sleep Mode, or Dashboard.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Pre-scan apps and schedule sync in background
        RestrictionManager.getInstance(this).scanAndSeedInstalledApps();
        ScheduleManager.getInstance(this).rescheduleAll();

        final Handler handler = new Handler(Looper.getMainLooper());
        final boolean[] navigated = {false};

        // 1. Asynchronously check GitHub for mandatory auto-updates
        UpdateManager.getInstance(this).checkForUpdate((updateAvailable, info, error) -> {
            if (isFinishing() || navigated[0]) return;
            if (updateAvailable && info != null) {
                navigated[0] = true;
                Intent updateIntent = new Intent(this, ForceUpdateActivity.class);
                updateIntent.putExtra(ForceUpdateActivity.EXTRA_UPDATE_INFO, info);
                startActivity(updateIntent);
                finish();
            }
        });

        // 2. Fallback timer if update check takes longer than 1500ms or device is offline
        handler.postDelayed(() -> {
            if (isFinishing() || navigated[0]) return;
            navigated[0] = true;

            PreferencesManager prefs = PreferencesManager.getInstance(this);
            if (!prefs.isOnboardingCompleted()) {
                startActivity(new Intent(this, OnboardingActivity.class));
            } else if (prefs.isSleepModeActive()) {
                startActivity(new Intent(this, SleepModeActiveActivity.class));
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
        }, 1500);
    }
}
