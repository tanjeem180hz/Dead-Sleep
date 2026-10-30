package com.deadsleep.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.deadsleep.app.R;
import com.deadsleep.app.data.PreferencesManager;
import com.deadsleep.app.manager.RestrictionManager;
import com.deadsleep.app.manager.ScheduleManager;
import com.deadsleep.app.manager.UpdateManager;
import com.deadsleep.app.util.AnimationHelper;

/**
 * Splash entry screen routing user to Automatic Update, Onboarding, Active Sleep Mode, or Dashboard.
 * Checks GitHub for mandatory updates immediately upon opening the application.
 */
public class SplashActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final boolean[] navigated = {false};
    private Runnable timeoutRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Pre-scan apps and schedule sync in background
        RestrictionManager.getInstance(this).scanAndSeedInstalledApps();
        ScheduleManager.getInstance(this).rescheduleAll();

        View logo = findViewById(R.id.ivSplashLogo);
        if (logo != null) {
            AnimationHelper.startPulseBreathing(logo);
        }

        // 1. Asynchronously check GitHub for updates IMMEDIATELY on app open
        UpdateManager.getInstance(this).checkForUpdate((updateAvailable, info, error) -> {
            if (isFinishing() || isDestroyed() || navigated[0]) return;
            if (updateAvailable && info != null) {
                navigated[0] = true;
                cancelTimeout();
                Intent updateIntent = new Intent(this, ForceUpdateActivity.class);
                updateIntent.putExtra(ForceUpdateActivity.EXTRA_UPDATE_INFO, info);
                updateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(updateIntent);
                finish();
            } else {
                // If update check returned and no update is pending, enter app immediately
                proceedToApp();
            }
        });

        // 2. Fallback timeout in case device is offline or network takes longer than 2200ms
        timeoutRunnable = this::proceedToApp;
        handler.postDelayed(timeoutRunnable, 2200);
    }

    private void proceedToApp() {
        if (isFinishing() || isDestroyed() || navigated[0]) return;
        navigated[0] = true;
        cancelTimeout();

        PreferencesManager prefs = PreferencesManager.getInstance(this);
        if (!prefs.isOnboardingCompleted()) {
            startActivity(new Intent(this, OnboardingActivity.class));
        } else if (prefs.isSleepModeActive()) {
            startActivity(new Intent(this, SleepModeActiveActivity.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }

    private void cancelTimeout() {
        if (timeoutRunnable != null) {
            handler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimeout();
    }
}
