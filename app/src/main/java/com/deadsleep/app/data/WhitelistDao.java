package com.deadsleep.app.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.deadsleep.app.model.WhitelistEntry;

import java.util.List;

/**
 * Data Access Object for WhitelistEntry table.
 */
@Dao
public interface WhitelistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(WhitelistEntry entry);

    @Delete
    void delete(WhitelistEntry entry);

    @Query("SELECT * FROM whitelist_entries ORDER BY displayName ASC")
    LiveData<List<WhitelistEntry>> getAllEntriesLiveData();

    @Query("SELECT * FROM whitelist_entries ORDER BY displayName ASC")
    List<WhitelistEntry> getAllEntriesSync();

    @Query("SELECT * FROM whitelist_entries WHERE identifier = :identifier LIMIT 1")
    WhitelistEntry getEntryByIdentifier(String identifier);

    @Query("SELECT COUNT(*) FROM whitelist_entries WHERE identifier = :identifier")
    int countByIdentifier(String identifier);
}
