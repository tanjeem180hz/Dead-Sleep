package com.deadsleep.app.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.deadsleep.app.model.BlockedAttempt;
import com.deadsleep.app.model.ChallengeResult;
import com.deadsleep.app.model.RestrictedApp;
import com.deadsleep.app.model.SleepSchedule;
import com.deadsleep.app.model.SleepSession;
import com.deadsleep.app.model.WhitelistEntry;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Clean repository abstraction for database access.
 */
public class DeadSleepRepository {

    private static volatile DeadSleepRepository INSTANCE;
    private final AppDatabase database;
    private final ExecutorService executor;

    private DeadSleepRepository(Context context) {
        this.database = AppDatabase.getInstance(context);
        this.executor = AppDatabase.getExecutor();
    }

    public static DeadSleepRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DeadSleepRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DeadSleepRepository(context);
                }
            }
        }
        return INSTANCE;
    }

    // Schedule Operations
    public LiveData<List<SleepSchedule>> getAllSchedulesLiveData() {
        return database.sleepScheduleDao().getAllSchedulesLiveData();
    }

    public List<SleepSchedule> getAllSchedulesSync() {
        return database.sleepScheduleDao().getAllSchedulesSync();
    }

    public List<SleepSchedule> getEnabledSchedulesSync() {
        return database.sleepScheduleDao().getEnabledSchedulesSync();
    }

    public SleepSchedule getScheduleById(long id) {
        return database.sleepScheduleDao().getScheduleById(id);
    }

    public void insertSchedule(SleepSchedule schedule, OnResultListener<Long> listener) {
        executor.execute(() -> {
            long id = database.sleepScheduleDao().insert(schedule);
            if (listener != null) listener.onResult(id);
        });
    }

    public void updateSchedule(SleepSchedule schedule) {
        executor.execute(() -> database.sleepScheduleDao().update(schedule));
    }

    public void deleteSchedule(SleepSchedule schedule) {
        executor.execute(() -> database.sleepScheduleDao().delete(schedule));
    }

    // Restricted App Operations
    public LiveData<List<RestrictedApp>> getAllAppsLiveData() {
        return database.restrictedAppDao().getAllAppsLiveData();
    }

    public List<RestrictedApp> getAllAppsSync() {
        return database.restrictedAppDao().getAllAppsSync();
    }

    public List<RestrictedApp> getRestrictedAppsSync() {
        return database.restrictedAppDao().getRestrictedAppsSync();
    }

    public LiveData<List<RestrictedApp>> getRestrictedAppsLiveData() {
        return database.restrictedAppDao().getRestrictedAppsLiveData();
    }

    public void insertOrUpdateApp(RestrictedApp app) {
        executor.execute(() -> database.restrictedAppDao().insertOrUpdate(app));
    }

    public void insertAllApps(List<RestrictedApp> apps) {
        executor.execute(() -> database.restrictedAppDao().insertAll(apps));
    }

    public void setRestrictedForCategory(String category, boolean isRestricted) {
        executor.execute(() -> database.restrictedAppDao().setRestrictedForCategory(category, isRestricted));
    }

    public void incrementBlockCount(String packageName) {
        executor.execute(() -> database.restrictedAppDao().incrementBlockCount(packageName));
    }

    // Whitelist Operations
    public LiveData<List<WhitelistEntry>> getWhitelistLiveData() {
        return database.whitelistDao().getAllEntriesLiveData();
    }

    public List<WhitelistEntry> getWhitelistSync() {
        return database.whitelistDao().getAllEntriesSync();
    }

    public void insertWhitelistEntry(WhitelistEntry entry) {
        executor.execute(() -> database.whitelistDao().insert(entry));
    }

    public void deleteWhitelistEntry(WhitelistEntry entry) {
        executor.execute(() -> database.whitelistDao().delete(entry));
    }

    public boolean isWhitelistedSync(String identifier) {
        return database.whitelistDao().countByIdentifier(identifier) > 0;
    }

    // Sleep Session Operations
    public LiveData<List<SleepSession>> getAllSessionsLiveData() {
        return database.sleepSessionDao().getAllSessionsLiveData();
    }

    public List<SleepSession> getRecentSessionsSync(int limit) {
        return database.sleepSessionDao().getRecentSessionsSync(limit);
    }

    public SleepSession getActiveSessionSync() {
        return database.sleepSessionDao().getActiveSessionSync();
    }

    public long insertSessionSync(SleepSession session) {
        return database.sleepSessionDao().insert(session);
    }

    public void updateSession(SleepSession session) {
        executor.execute(() -> database.sleepSessionDao().update(session));
    }

    public void updateSessionSync(SleepSession session) {
        database.sleepSessionDao().update(session);
    }

    // Challenge Result Operations
    public LiveData<List<ChallengeResult>> getAllResultsLiveData() {
        return database.challengeResultDao().getAllResultsLiveData();
    }

    public List<ChallengeResult> getRecentResultsSync(int limit) {
        return database.challengeResultDao().getRecentResultsSync(limit);
    }

    public void insertChallengeResult(ChallengeResult result) {
        executor.execute(() -> database.challengeResultDao().insert(result));
    }

    public Double getAverageSolveTimeSecSync() {
        return database.challengeResultDao().getAverageSolveTimeSec();
    }

    // Blocked Attempt Operations
    public LiveData<List<BlockedAttempt>> getAllBlockedAttemptsLiveData() {
        return database.blockedAttemptDao().getAllAttemptsLiveData();
    }

    public void insertBlockedAttempt(BlockedAttempt attempt) {
        executor.execute(() -> {
            database.blockedAttemptDao().insert(attempt);
            database.restrictedAppDao().incrementBlockCount(attempt.getPackageName());
        });
    }

    public List<BlockedAttempt> getBlockedAttemptsSinceSync(long sinceMillis) {
        return database.blockedAttemptDao().getAttemptsSinceSync(sinceMillis);
    }

    public int getTotalBlockedCountSync() {
        return database.blockedAttemptDao().getTotalBlockedCount();
    }

    public String getMostBlockedPackage() {
        return database.blockedAttemptDao().getMostBlockedPackage();
    }

    public interface OnResultListener<T> {
        void onResult(T result);
    }
}
