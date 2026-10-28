package com.deadsleep.app.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.deadsleep.app.model.SleepSession;

import java.util.List;

/**
 * Data Access Object for SleepSession table.
 */
@Dao
public interface SleepSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SleepSession session);

    @Update
    void update(SleepSession session);

    @Query("SELECT * FROM sleep_sessions ORDER BY startTimeMillis DESC")
    LiveData<List<SleepSession>> getAllSessionsLiveData();

    @Query("SELECT * FROM sleep_sessions ORDER BY startTimeMillis DESC LIMIT :limit")
    List<SleepSession> getRecentSessionsSync(int limit);

    @Query("SELECT * FROM sleep_sessions WHERE id = :sessionId LIMIT 1")
    SleepSession getSessionById(long sessionId);

    @Query("SELECT * FROM sleep_sessions WHERE status = 'ACTIVE' ORDER BY startTimeMillis DESC LIMIT 1")
    SleepSession getActiveSessionSync();

    @Query("SELECT COUNT(*) FROM sleep_sessions WHERE isCompleted = 1")
    int getCompletedSessionCount();

    @Query("DELETE FROM sleep_sessions")
    void clearAll();
}
