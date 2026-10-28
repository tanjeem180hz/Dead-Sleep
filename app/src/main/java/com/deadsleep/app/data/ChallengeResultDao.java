package com.deadsleep.app.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.deadsleep.app.model.ChallengeResult;

import java.util.List;

/**
 * Data Access Object for ChallengeResult table.
 */
@Dao
public interface ChallengeResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ChallengeResult result);

    @Query("SELECT * FROM challenge_results ORDER BY timestamp DESC")
    LiveData<List<ChallengeResult>> getAllResultsLiveData();

    @Query("SELECT * FROM challenge_results ORDER BY timestamp DESC LIMIT :limit")
    List<ChallengeResult> getRecentResultsSync(int limit);

    @Query("SELECT AVG(timeTakenSec) FROM challenge_results WHERE isPassed = 1")
    Double getAverageSolveTimeSec();
}
