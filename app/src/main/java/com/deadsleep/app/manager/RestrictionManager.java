package com.deadsleep.app.manager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.deadsleep.app.data.AppDatabase;
import com.deadsleep.app.data.DeadSleepRepository;
import com.deadsleep.app.model.RestrictedApp;
import com.deadsleep.app.model.WhitelistEntry;
import com.deadsleep.app.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages restricted app detection, categorization, whitelist exemptions, and smart defaults.
 */
public class RestrictionManager {

    private static volatile RestrictionManager INSTANCE;
    private final Context context;
    private final DeadSleepRepository repository;

    // Critical system packages that are NEVER blocked under any condition
    private static final Set<String> IMMUNE_PACKAGES = new HashSet<>(Arrays.asList(
            "com.deadsleep.app",
            "android",
            "com.android.systemui",
            "com.android.phone",
            "com.android.server.telecom",
            "com.google.android.dialer",
            "com.samsung.android.dialer",
            "com.android.dialer",
            "com.android.emergency",
            "com.google.android.deskclock",
            "com.android.deskclock",
            "com.sec.android.app.clockpackage"
    ));

    // Smart default suggestions
    private static final Set<String> KNOWN_SOCIAL = new HashSet<>(Arrays.asList(
            "com.facebook.katana", "com.instagram.android", "com.zhiliaoapp.musically",
            "com.snapchat.android", "com.twitter.android", "com.reddit.frontpage",
            "com.linkedin.android", "com.pinterest"
    ));

    private static final Set<String> KNOWN_VIDEO = new HashSet<>(Arrays.asList(
            "com.google.android.youtube", "com.netflix.mediaclient", "com.disney.disneyplus",
            "tv.twitch.android.app", "com.amazon.avod.thirdpartyclient", "com.hulu.plus"
    ));

    private static final Set<String> KNOWN_BROWSERS = new HashSet<>(Arrays.asList(
            "com.android.chrome", "org.mozilla.firefox", "com.opera.browser",
            "com.microsoft.emmx", "com.sec.android.app.sbrowser", "com.brave.browser"
    ));

    private RestrictionManager(Context context) {
        this.context = context.getApplicationContext();
        this.repository = DeadSleepRepository.getInstance(this.context);
    }

    public static RestrictionManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (RestrictionManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RestrictionManager(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Fast synchronous check to determine if an app should be blocked.
     */
    public boolean isPackageRestricted(String packageName) {
        if (packageName == null || packageName.isEmpty()) return false;

        // 1. Immune critical packages
        if (IMMUNE_PACKAGES.contains(packageName)) return false;

        // 2. Check Whitelist
        if (repository.isWhitelistedSync(packageName)) return false;

        // 3. Check Restricted Apps table
        List<RestrictedApp> restrictedList = repository.getRestrictedAppsSync();
        for (RestrictedApp app : restrictedList) {
            if (app.getPackageName().equals(packageName) && app.isRestricted()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Scans installed launchable applications and seeds the restricted apps database with smart suggestions.
     */
    public void scanAndSeedInstalledApps() {
        AppDatabase.getExecutor().execute(() -> {
            PackageManager pm = context.getPackageManager();
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);

            List<RestrictedApp> appsToSave = new ArrayList<>();
            for (ResolveInfo ri : activities) {
                String pkg = ri.activityInfo.packageName;
                if (pkg.equals(context.getPackageName())) continue;

                String appName = ri.loadLabel(pm).toString();
                String category = categorizeApp(pkg, ri.activityInfo.applicationInfo);
                boolean isPreChecked = shouldPreCheckApp(pkg, category);

                RestrictedApp restrictedApp = new RestrictedApp(pkg, appName, category, isPreChecked);
                appsToSave.add(restrictedApp);
            }

            repository.insertAllApps(appsToSave);
        });
    }

    private String categorizeApp(String packageName, ApplicationInfo appInfo) {
        if (KNOWN_SOCIAL.contains(packageName)) return Constants.CATEGORY_SOCIAL;
        if (KNOWN_VIDEO.contains(packageName)) return Constants.CATEGORY_VIDEO;
        if (KNOWN_BROWSERS.contains(packageName)) return Constants.CATEGORY_BROWSERS;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int cat = appInfo.category;
            if (cat == ApplicationInfo.CATEGORY_SOCIAL) return Constants.CATEGORY_SOCIAL;
            if (cat == ApplicationInfo.CATEGORY_GAME) return Constants.CATEGORY_GAMES;
            if (cat == ApplicationInfo.CATEGORY_VIDEO) return Constants.CATEGORY_VIDEO;
        }

        if (packageName.toLowerCase().contains("game")) return Constants.CATEGORY_GAMES;
        return Constants.CATEGORY_OTHER;
    }

    private boolean shouldPreCheckApp(String packageName, String category) {
        return KNOWN_SOCIAL.contains(packageName)
                || KNOWN_VIDEO.contains(packageName)
                || KNOWN_BROWSERS.contains(packageName)
                || Constants.CATEGORY_GAMES.equals(category);
    }
}
