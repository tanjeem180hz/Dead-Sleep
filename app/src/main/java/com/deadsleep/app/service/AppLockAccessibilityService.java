package com.deadsleep.app.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.deadsleep.app.data.AppDatabase;
import com.deadsleep.app.data.PreferencesManager;
import com.deadsleep.app.manager.RestrictionManager;
import com.deadsleep.app.model.BlockedAttempt;
import com.deadsleep.app.ui.LockOverlayActivity;
import com.deadsleep.app.util.Constants;
import com.deadsleep.app.util.TimeUtils;

/**
 * Accessibility Service providing real-time app lockout and tamper protection during sleep mode.
 */
public class AppLockAccessibilityService extends AccessibilityService {

    private RestrictionManager restrictionManager;
    private PreferencesManager preferencesManager;
    private long lastBlockTimestamp = 0;
    private String lastBlockedPackage = "";

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        this.restrictionManager = RestrictionManager.getInstance(this);
        this.preferencesManager = PreferencesManager.getInstance(this);

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        if (preferencesManager == null) preferencesManager = PreferencesManager.getInstance(this);
        if (restrictionManager == null) restrictionManager = RestrictionManager.getInstance(this);

        // Only lock when sleep mode is active!
        if (!preferencesManager.isSleepModeActive()) return;

        CharSequence pkgSeq = event.getPackageName();
        String currentPackage = pkgSeq.toString();

        // 1. TAMPER PROTECTION
        // Prevent deactivating device admin, accessibility, or force-stopping/uninstalling Dead Sleep during sleep mode
        if (isTamperScreen(event, currentPackage)) {
            performGlobalAction(GLOBAL_ACTION_HOME);
            Toast.makeText(this, "🛡️ Dead Sleep is active! Settings are locked during bedtime.", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. RESTRICTED APP LOCKOUT
        if (restrictionManager.isPackageRestricted(currentPackage)) {
            long now = System.currentTimeMillis();
            // Debounce rapid continuous events from same app
            if (currentPackage.equals(lastBlockedPackage) && (now - lastBlockTimestamp < 1500)) {
                return;
            }
            lastBlockTimestamp = now;
            lastBlockedPackage = currentPackage;

            // Log attempt
            AppDatabase.getExecutor().execute(() -> {
                BlockedAttempt attempt = new BlockedAttempt(
                        currentPackage,
                        currentPackage,
                        now,
                        TimeUtils.formatDate(now)
                );
                com.deadsleep.app.data.DeadSleepRepository.getInstance(this).insertBlockedAttempt(attempt);
            });

            // Immediately launch full-screen "Blocked by Dead Sleep" overlay
            try {
                Intent lockIntent = new Intent(this, LockOverlayActivity.class);
                lockIntent.putExtra(Constants.EXTRA_BLOCKED_PACKAGE, currentPackage);
                lockIntent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP |
                        Intent.FLAG_ACTIVITY_NO_ANIMATION
                );
                startActivity(lockIntent);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Detects if the current screen is attempting to uninstall or kill Dead Sleep.
     */
    private boolean isTamperScreen(AccessibilityEvent event, String currentPackage) {
        if ("com.android.settings".equals(currentPackage)) {
            CharSequence className = event.getClassName();
            CharSequence text = event.getText() != null ? event.getText().toString() : "";

            // Check if App Info for Dead Sleep, Device Admin deactivation, or Accessibility toggle
            if (text != null && text.toString().toLowerCase().contains("dead sleep")) {
                return true;
            }
            if (className != null && className.toString().contains("DeviceAdminAdd")) {
                return true;
            }
        }

        if ("com.google.android.packageinstaller".equals(currentPackage)
                || "com.android.packageinstaller".equals(currentPackage)) {
            CharSequence text = event.getText() != null ? event.getText().toString() : "";
            if (text != null && text.toString().toLowerCase().contains("dead sleep")) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onInterrupt() {}
}
