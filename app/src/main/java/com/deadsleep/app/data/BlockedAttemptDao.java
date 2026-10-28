package com.deadsleep.app.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.deadsleep.app.model.BlockedAttempt;

import java.util.List;

/**
 * Data Access Object for BlockedAttempt table.
 */
@Dao
public interface BlockedAttemptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BlockedAttempt attempt);

    @Query("SELECT * FROM blocked_attempts ORDER BY timestampMillis DESC")
    LiveData<List<BlockedAttempt>> getAllAttemptsLiveData();

    @Query("SELECT * FROM blocked_attempts WHERE timestampMillis >= :sinceMillis ORDER BY timestampMillis DESC")
    List<BlockedAttempt> getAttemptsSinceSync(long sinceMillis);

    @Query("SELECT COUNT(*) FROM blocked_attempts")
    int getTotalBlockedCount();

    @Query("SELECT packageName FROM blocked_attempts GROUP BY packageName ORDER BY COUNT(*) DESC LIMIT 1")
    String getMostBlockedPackage();
}
