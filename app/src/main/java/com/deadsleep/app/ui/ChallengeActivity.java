package com.deadsleep.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.deadsleep.app.challenge.Challenge;
import com.deadsleep.app.challenge.ChallengeFactory;
import com.deadsleep.app.databinding.ActivityChallengeBinding;
import com.deadsleep.app.manager.ChallengeManager;
import com.deadsleep.app.manager.DeadSleepAlarmManager;
import com.deadsleep.app.util.Constants;

import java.util.Locale;

/**
 * Kiosk-like activity hosting cognitive challenges with anti-cheat protections.
 */
public class ChallengeActivity extends AppCompatActivity implements Challenge.ChallengeCallback {

    private ActivityChallengeBinding binding;
    private Challenge activeChallenge;
    private long startTimeMillis;
    private boolean isPractice = false;
    private long scheduleId = -1;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupWindowFlags();

        binding = ActivityChallengeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isPractice = getIntent().getBooleanExtra(Constants.EXTRA_IS_PRACTICE, false);
        scheduleId = getIntent().getLongExtra(Constants.EXTRA_SCHEDULE_ID, -1L);

        if (isPractice) {
            binding.tvPracticeBanner.setVisibility(View.VISIBLE);
        }

        setupChallenge();
        startStopwatch();

        // Block back button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isPractice) {
                    finish();
                } else {
                    Toast.makeText(ChallengeActivity.this, "Solve the challenge to turn off the alarm!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setupChallenge() {
        int snoozeCount = DeadSleepAlarmManager.getInstance(this).getCurrentSnoozeCount();
        String preferredType = getIntent().getStringExtra(Constants.EXTRA_CHALLENGE_TYPE);
        String difficulty = getIntent().getStringExtra(Constants.EXTRA_CHALLENGE_DIFFICULTY);

        if (isPractice && preferredType != null) {
            activeChallenge = ChallengeFactory.createChallenge(this, preferredType, difficulty);
        } else {
            activeChallenge = ChallengeManager.getInstance(this).getChallengeForAlarm(scheduleId, snoozeCount);
        }

        binding.tvChallengeTitle.setText(activeChallenge.getTitle());
        binding.tvChallengeInstructions.setText(activeChallenge.getInstructions());

        View challengeView = activeChallenge.renderView(this, binding.challengeContainer, this);
        binding.challengeContainer.removeAllViews();
        binding.challengeContainer.addView(challengeView);
    }

    private void startStopwatch() {
        startTimeMillis = System.currentTimeMillis();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = (System.currentTimeMillis() - startTimeMillis) / 1000;
                long mins = elapsed / 60;
                long secs = elapsed % 60;
                binding.tvChallengeTimer.setText(String.format(Locale.US, "%02d:%02d", mins, secs));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    @Override
    public void onChallengeSolved() {
        stopStopwatch();
        int elapsedSec = (int) ((System.currentTimeMillis() - startTimeMillis) / 1000);

        if (isPractice) {
            Toast.makeText(this, "🎉 Challenge Solved in " + elapsedSec + "s! Great practice.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Record stats and stop alarm
        ChallengeManager.getInstance(this).recordResult(
                activeChallenge.getTitle(),
                activeChallenge.getDifficulty().name(),
                elapsedSec,
                1,
                true
        );

        DeadSleepAlarmManager.getInstance(this).dismissAlarm(elapsedSec);

        Intent gmIntent = new Intent(this, GoodMorningActivity.class);
        gmIntent.putExtra("solve_time_sec", elapsedSec);
        startActivity(gmIntent);
        finish();
    }

    @Override
    public void onChallengeFailed(String reason) {
        Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProgressUpdated(int current, int total) {
        // Updated by individual challenges
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (!isPractice) {
            // Anti-cheat: relaunch challenge activity if user presses home
            Intent reopen = new Intent(this, ChallengeActivity.class);
            reopen.putExtra(Constants.EXTRA_SCHEDULE_ID, scheduleId);
            reopen.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(reopen);
        }
    }

    private void stopStopwatch() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
            timerRunnable = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopStopwatch();
        if (activeChallenge != null) {
            activeChallenge.cleanup();
        }
    }
}
