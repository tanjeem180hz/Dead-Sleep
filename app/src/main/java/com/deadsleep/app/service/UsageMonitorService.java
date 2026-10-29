package com.deadsleep.app.service;

import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.deadsleep.app.data.PreferencesManager;
import com.deadsleep.app.manager.RestrictionManager;
import com.deadsleep.app.ui.LockOverlayActivity;
import com.deadsleep.app.util.Constants;

/**
 * Backup foreground app monitor using UsageStatsManager.
 */
public class UsageMonitorService extends Service {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable monitorRunnable;
    private UsageStatsManager usageStatsManager;
    private RestrictionManager restrictionManager;
    private PreferencesManager preferencesManager;
    private String lastForeground = "";

    @Override
    public void onCreate() {
        super.onCreate();
        this.usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        this.restrictionManager = RestrictionManager.getInstance(this);
        this.preferencesManager = PreferencesManager.getInstance(this);

        monitorRunnable = new Runnable() {
            @Override
            public void run() {
                if (preferencesManager.isSleepModeActive()) {
                    checkForegroundApp();
                }
                handler.postDelayed(this, 1500); // Check every 1.5s
            }
        };
        handler.post(monitorRunnable);
    }

    private void checkForegroundApp() {
        if (usageStatsManager == null) return;
        try {
            long time = System.currentTimeMillis();
            UsageEvents usageEvents = usageStatsManager.queryEvents(time - 3000, time);
            if (usageEvents == null) return;
            UsageEvents.Event event = new UsageEvents.Event();

            String currentApp = null;
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                    currentApp = event.getPackageName();
                }
            }

            if (currentApp != null && !currentApp.equals(lastForeground)) {
                lastForeground = currentApp;
                if (restrictionManager.isPackageRestricted(currentApp)) {
                    Intent lockIntent = new Intent(this, LockOverlayActivity.class);
                    lockIntent.putExtra(Constants.EXTRA_BLOCKED_PACKAGE, currentApp);
                    lockIntent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP |
                            Intent.FLAG_ACTIVITY_NO_ANIMATION
                    );
                    startActivity(lockIntent);
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (monitorRunnable != null) {
            handler.removeCallbacks(monitorRunnable);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
