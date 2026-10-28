package com.deadsleep.app.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.deadsleep.app.model.SleepSchedule;

import java.util.List;

/**
 * Data Access Object for SleepSchedule table.
 */
@Dao
public interface SleepScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SleepSchedule schedule);

    @Update
    void update(SleepSchedule schedule);

    @Delete
    void delete(SleepSchedule schedule);

    @Query("SELECT * FROM sleep_schedules ORDER BY sleepHour ASC, sleepMinute ASC")
    LiveData<List<SleepSchedule>> getAllSchedulesLiveData();

    @Query("SELECT * FROM sleep_schedules ORDER BY sleepHour ASC, sleepMinute ASC")
    List<SleepSchedule> getAllSchedulesSync();

    @Query("SELECT * FROM sleep_schedules WHERE isEnabled = 1 ORDER BY sleepHour ASC, sleepMinute ASC")
    List<SleepSchedule> getEnabledSchedulesSync();

    @Query("SELECT * FROM sleep_schedules WHERE id = :id LIMIT 1")
    SleepSchedule getScheduleById(long id);

    @Query("SELECT COUNT(*) FROM sleep_schedules")
    int getScheduleCount();
}
