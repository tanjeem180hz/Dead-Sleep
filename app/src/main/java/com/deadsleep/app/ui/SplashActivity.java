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

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing()) return;

            PreferencesManager prefs = PreferencesManager.getInstance(this);
            if (!prefs.isOnboardingCompleted()) {
                startActivity(new Intent(this, OnboardingActivity.class));
            } else if (prefs.isSleepModeActive()) {
                startActivity(new Intent(this, SleepModeActiveActivity.class));
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
        }, 1200);
    }
}
