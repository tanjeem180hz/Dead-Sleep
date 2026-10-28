package com.deadsleep.app;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.deadsleep.app.data.PreferencesManager;
import com.deadsleep.app.ui.MainActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

/**
 * Instrumented UI test verifying MainActivity launching and basic state initialization.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        PreferencesManager prefs = PreferencesManager.getInstance(context);
        prefs.setOnboardingComplete(true);
    }

    @Test
    public void testMainActivityLaunchesSuccessfully() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                assertNotNull("MainActivity should not be null", activity);
            });
        }
    }
}
