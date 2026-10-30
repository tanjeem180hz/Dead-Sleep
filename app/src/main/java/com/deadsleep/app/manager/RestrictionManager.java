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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
            "com.facebook.katana", "com.facebook.orca", "com.instagram.android",
            "com.zhiliaoapp.musically", "com.snapchat.android", "com.twitter.android",
            "com.reddit.frontpage", "com.linkedin.android", "com.pinterest",
            "org.telegram.messenger", "com.whatsapp", "com.whatsapp.w4b",
            "com.discord", "com.viber.voip", "com.tencent.mm"
    ));

    private static final Set<String> KNOWN_VIDEO = new HashSet<>(Arrays.asList(
            "com.google.android.youtube", "com.netflix.mediaclient", "com.disney.disneyplus",
            "tv.twitch.android.app", "com.amazon.avod.thirdpartyclient", "com.hulu.plus",
            "com.mxtech.videoplayer.ad", "com.mxtech.videoplayer.pro"
    ));

    private static final Set<String> KNOWN_BROWSERS = new HashSet<>(Arrays.asList(
            "com.android.chrome", "org.mozilla.firefox", "com.opera.browser",
            "com.opera.mini.native", "com.microsoft.emmx", "com.sec.android.app.sbrowser",
            "com.brave.browser", "com.UCMobile.intl"
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
     * Scans all installed launchable and user applications on the device,
     * seeds the restricted apps database with smart suggestions, and preserves existing user selections.
     */
    public void scanAndSeedInstalledApps() {
        AppDatabase.getExecutor().execute(() -> {
            try {
                PackageManager pm = context.getPackageManager();
                if (pm == null) return;

                // Load existing preferences to preserve user toggles and block counts
                List<RestrictedApp> existingList = repository.getAllAppsSync();
                Map<String, RestrictedApp> existingMap = new HashMap<>();
                if (existingList != null) {
                    for (RestrictedApp a : existingList) {
                        existingMap.put(a.getPackageName(), a);
                    }
                }

                Map<String, RestrictedApp> scannedMap = new HashMap<>();

                // 1. Pass 1: Query all launcher intent activities
                Intent launcherIntent = new Intent(Intent.ACTION_MAIN, null);
                launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                int queryFlags = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    queryFlags |= PackageManager.MATCH_ALL;
                }
                List<ResolveInfo> activities = pm.queryIntentActivities(launcherIntent, queryFlags);
                if (activities != null) {
                    for (ResolveInfo ri : activities) {
                        if (ri == null || ri.activityInfo == null || ri.activityInfo.packageName == null) continue;
                        String pkg = ri.activityInfo.packageName;
                        if (pkg.equals(context.getPackageName()) || IMMUNE_PACKAGES.contains(pkg)) continue;

                        CharSequence labelCs = ri.loadLabel(pm);
                        String appName = (labelCs != null && labelCs.length() > 0) ? labelCs.toString() : pkg;
                        String category = categorizeApp(pkg, ri.activityInfo.applicationInfo);

                        boolean isRestricted;
                        int blockCount = 0;
                        if (existingMap.containsKey(pkg)) {
                            RestrictedApp prev = existingMap.get(pkg);
                            isRestricted = prev != null && prev.isRestricted();
                            blockCount = prev != null ? prev.getBlockCount() : 0;
                        } else {
                            isRestricted = shouldPreCheckApp(pkg, category);
                        }

                        RestrictedApp restrictedApp = new RestrictedApp(pkg, appName, category, isRestricted);
                        restrictedApp.setBlockCount(blockCount);
                        scannedMap.put(pkg, restrictedApp);
                    }
                }

                // 2. Pass 2: Query all installed applications to capture any user or updated system apps
                int appFlags = PackageManager.GET_META_DATA;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    appFlags |= PackageManager.MATCH_ALL;
                }
                List<ApplicationInfo> installedApps = pm.getInstalledApplications(appFlags);
                if (installedApps != null) {
                    for (ApplicationInfo ai : installedApps) {
                        if (ai == null || ai.packageName == null) continue;
                        String pkg = ai.packageName;
                        if (pkg.equals(context.getPackageName()) || IMMUNE_PACKAGES.contains(pkg)) continue;
                        if (scannedMap.containsKey(pkg)) continue; // Already added in pass 1

                        boolean isUserApp = (ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
                        boolean isUpdatedSystemApp = (ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
                        boolean hasLaunchIntent = pm.getLaunchIntentForPackage(pkg) != null;

                        // Include any user app, updated system app, or app with launch intent
                        if (isUserApp || isUpdatedSystemApp || hasLaunchIntent) {
                            CharSequence labelCs = ai.loadLabel(pm);
                            String appName = (labelCs != null && labelCs.length() > 0) ? labelCs.toString() : pkg;
                            String category = categorizeApp(pkg, ai);

                            boolean isRestricted;
                            int blockCount = 0;
                            if (existingMap.containsKey(pkg)) {
                                RestrictedApp prev = existingMap.get(pkg);
                                isRestricted = prev != null && prev.isRestricted();
                                blockCount = prev != null ? prev.getBlockCount() : 0;
                            } else {
                                isRestricted = shouldPreCheckApp(pkg, category);
                            }

                            RestrictedApp restrictedApp = new RestrictedApp(pkg, appName, category, isRestricted);
                            restrictedApp.setBlockCount(blockCount);
                            scannedMap.put(pkg, restrictedApp);
                        }
                    }
                }

                if (!scannedMap.isEmpty()) {
                    repository.insertAllApps(new ArrayList<>(scannedMap.values()));
                }
            } catch (Exception ignored) {}
        });
    }

    private String categorizeApp(String packageName, ApplicationInfo appInfo) {
        String lowerPkg = packageName.toLowerCase(Locale.US);

        if (KNOWN_SOCIAL.contains(packageName)
                || lowerPkg.contains("facebook") || lowerPkg.contains("instagram")
                || lowerPkg.contains("twitter") || lowerPkg.contains("tiktok")
                || lowerPkg.contains("snapchat") || lowerPkg.contains("whatsapp")
                || lowerPkg.contains("telegram") || lowerPkg.contains("discord")
                || lowerPkg.contains("reddit") || lowerPkg.contains("messenger")) {
            return Constants.CATEGORY_SOCIAL;
        }

        if (KNOWN_VIDEO.contains(packageName)
                || lowerPkg.contains("youtube") || lowerPkg.contains("netflix")
                || lowerPkg.contains("twitch") || lowerPkg.contains("disney")
                || lowerPkg.contains("hulu") || lowerPkg.contains("primevideo")
                || lowerPkg.contains("video") || lowerPkg.contains("stream")) {
            return Constants.CATEGORY_VIDEO;
        }

        if (KNOWN_BROWSERS.contains(packageName)
                || lowerPkg.contains("browser") || lowerPkg.contains("chrome")
                || lowerPkg.contains("firefox") || lowerPkg.contains("opera")
                || lowerPkg.contains("edge")) {
            return Constants.CATEGORY_BROWSERS;
        }

        if (appInfo != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int cat = appInfo.category;
                if (cat == ApplicationInfo.CATEGORY_SOCIAL) return Constants.CATEGORY_SOCIAL;
                if (cat == ApplicationInfo.CATEGORY_GAME) return Constants.CATEGORY_GAMES;
                if (cat == ApplicationInfo.CATEGORY_VIDEO) return Constants.CATEGORY_VIDEO;
            }
        }

        if (lowerPkg.contains("game") || lowerPkg.contains("play")
                || lowerPkg.contains("puzzle") || lowerPkg.contains("clash")
                || lowerPkg.contains("pubg") || lowerPkg.contains("freefire")
                || lowerPkg.contains("candycrush") || lowerPkg.contains("roblox")
                || lowerPkg.contains("subway") || lowerPkg.contains("ludo")) {
            return Constants.CATEGORY_GAMES;
        }

        return Constants.CATEGORY_OTHER;
    }

    private boolean shouldPreCheckApp(String packageName, String category) {
        return KNOWN_SOCIAL.contains(packageName)
                || KNOWN_VIDEO.contains(packageName)
                || KNOWN_BROWSERS.contains(packageName)
                || Constants.CATEGORY_GAMES.equals(category)
                || Constants.CATEGORY_SOCIAL.equals(category);
    }
}
