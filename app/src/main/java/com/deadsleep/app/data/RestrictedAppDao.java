package com.deadsleep.app.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.deadsleep.app.model.RestrictedApp;

import java.util.List;

/**
 * Data Access Object for RestrictedApp table.
 */
@Dao
public interface RestrictedAppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(RestrictedApp app);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RestrictedApp> apps);

    @Update
    void update(RestrictedApp app);

    @Delete
    void delete(RestrictedApp app);

    @Query("SELECT * FROM restricted_apps ORDER BY appName ASC")
    LiveData<List<RestrictedApp>> getAllAppsLiveData();

    @Query("SELECT * FROM restricted_apps ORDER BY appName ASC")
    List<RestrictedApp> getAllAppsSync();

    @Query("SELECT * FROM restricted_apps WHERE isRestricted = 1")
    List<RestrictedApp> getRestrictedAppsSync();

    @Query("SELECT * FROM restricted_apps WHERE isRestricted = 1")
    LiveData<List<RestrictedApp>> getRestrictedAppsLiveData();

    @Query("SELECT * FROM restricted_apps WHERE packageName = :packageName LIMIT 1")
    RestrictedApp getAppByPackage(String packageName);

    @Query("UPDATE restricted_apps SET blockCount = blockCount + 1 WHERE packageName = :packageName")
    void incrementBlockCount(String packageName);

    @Query("UPDATE restricted_apps SET isRestricted = :isRestricted WHERE category = :category")
    void setRestrictedForCategory(String category, boolean isRestricted);

    @Query("SELECT COUNT(*) FROM restricted_apps WHERE isRestricted = 1")
    int getRestrictedCount();
}
