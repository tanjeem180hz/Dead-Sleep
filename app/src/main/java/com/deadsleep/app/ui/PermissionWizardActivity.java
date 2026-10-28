package com.deadsleep.app.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.deadsleep.app.R;
import com.deadsleep.app.util.PermissionUtils;

import java.util.List;

/**
 * Interactive permission wizard guiding user through enabling required permissions.
 */
public class PermissionWizardActivity extends AppCompatActivity {

    private LinearLayout layoutPermissionsList;
    private Button btnContinue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_wizard);

        layoutPermissionsList = findViewById(R.id.layoutPermissionsList);
        btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(v -> {
            com.deadsleep.app.data.PreferencesManager.getInstance(this).setOnboardingCompleted(true);
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPermissionsList();
    }

    private void refreshPermissionsList() {
        layoutPermissionsList.removeAllViews();
        List<PermissionUtils.PermissionItem> items = PermissionUtils.getPermissionHealthList(this);

        for (PermissionUtils.PermissionItem item : items) {
            View card = LayoutInflater.from(this).inflate(R.layout.item_permission_card, layoutPermissionsList, false);

            TextView tvTitle = card.findViewById(R.id.tvPermTitle);
            TextView tvDesc = card.findViewById(R.id.tvPermDescription);
            Button btnAction = card.findViewById(R.id.btnPermAction);

            tvTitle.setText(item.title);
            tvDesc.setText(item.description);

            if (item.isGranted) {
                btnAction.setText("✓ Granted");
                btnAction.setEnabled(false);
                btnAction.setTextColor(Color.parseColor("#69F0AE"));
                btnAction.setBackgroundColor(Color.parseColor("#1B3322"));
            } else {
                btnAction.setText("Enable");
                btnAction.setEnabled(true);
                btnAction.setTextColor(Color.WHITE);
                btnAction.setBackgroundColor(Color.parseColor("#35248A"));

                btnAction.setOnClickListener(v -> {
                    if ("accessibility".equals(item.id)) {
                        showAccessibilityDisclosure(item.settingsIntent);
                    } else if (item.settingsIntent != null) {
                        try {
                            startActivity(item.settingsIntent);
                        } catch (Exception e) {
                            // Fallback to app details
                            startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    android.net.Uri.parse("package:" + getPackageName())));
                        }
                    }
                });
            }

            layoutPermissionsList.addView(card);
        }
    }

    /**
     * Shows a prominent disclosure dialog for Accessibility Service compliance.
     */
    private void showAccessibilityDisclosure(Intent targetIntent) {
        new AlertDialog.Builder(this)
                .setTitle("Accessibility Permission Disclosure 🛡️")
                .setMessage("Dead Sleep uses Android's Accessibility Service solely to:\n\n" +
                        "1. Detect when restricted distracting apps (games, social media) are opened during active sleep hours so they can be locked.\n" +
                        "2. Prevent unauthorized uninstallation or force-stopping while sleep mode is active.\n\n" +
                        "Dead Sleep is 100% offline. No keystrokes, personal data, or activity history is ever tracked, collected, or transmitted.")
                .setPositiveButton("I Understand & Enable", (dialog, which) -> {
                    try {
                        startActivity(targetIntent);
                    } catch (Exception e) {
                        startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
