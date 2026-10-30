package com.deadsleep.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.deadsleep.app.R;
import com.deadsleep.app.data.PreferencesManager;
import com.deadsleep.app.ui.adapter.OnboardingAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Onboarding carousel explaining the three core pillars: Plan, Restrict, and Awaken.
 */
public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnNext, btnSkip;
    private OnboardingAdapter adapter;
    private List<OnboardingAdapter.PageItem> pages;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPagerOnboarding);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);

        pages = new ArrayList<>();
        pages.add(new OnboardingAdapter.PageItem(
                "🌙",
                "Phase 1: Plan",
                "Define your target sleep and wake times with weekly repeat rules. Receive gentle bedtime reminders before sleep mode begins."
        ));
        pages.add(new OnboardingAdapter.PageItem(
                "🚫",
                "Phase 2: Restrict",
                "Distracting apps (social media, games, streaming) lock automatically at bedtime. Dead Sleep protects itself from tamper or uninstallation during sleep hours."
        ));
        pages.add(new OnboardingAdapter.PageItem(
                "🧠",
                "Phase 3: Awaken",
                "No more mindless snooze taps. The morning wake alarm will ONLY stop when you prove you are awake by solving a cognitive puzzle."
        ));
        pages.add(new OnboardingAdapter.PageItem(
                "☀️",
                "Ready to Sleep Better?",
                "Let's configure essential system permissions so Dead Sleep can protect your sleep routine and wake you up on time."
        ));

        adapter = new OnboardingAdapter(pages);
        viewPager.setAdapter(adapter);

        // Smooth Page Zoom Transformer
        viewPager.setPageTransformer((page, position) -> {
            if (position < -1) {
                page.setAlpha(0f);
            } else if (position <= 1) {
                float factor = Math.max(0.85f, 1 - Math.abs(position));
                page.setScaleX(factor);
                page.setScaleY(factor);
                page.setAlpha(Math.max(0.4f, 1 - Math.abs(position)));
            } else {
                page.setAlpha(0f);
            }
        });

        com.deadsleep.app.util.AnimationHelper.attachBounceFeedback(btnNext);
        com.deadsleep.app.util.AnimationHelper.attachBounceFeedback(btnSkip);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == pages.size() - 1) {
                    btnNext.setText("Get Started →");
                } else {
                    btnNext.setText("Next");
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < pages.size() - 1) {
                viewPager.setCurrentItem(current + 1);
            } else {
                completeOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> completeOnboarding());
    }

    private void completeOnboarding() {
        PreferencesManager.getInstance(this).setOnboardingCompleted(true);
        startActivity(new Intent(this, PermissionWizardActivity.class));
        finish();
    }
}
