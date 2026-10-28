package com.deadsleep.app.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents an explicitly allowed app or emergency contact that bypasses sleep restrictions.
 */
@Entity(tableName = "whitelist_entries")
public class WhitelistEntry {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String identifier; // package name or phone number
    private String type; // APP or CONTACT
    private String displayName;
    private boolean isEmergency; // e.g. Phone dialer, 911, 999, 112

    public WhitelistEntry(String identifier, String type, String displayName, boolean isEmergency) {
        this.identifier = identifier;
        this.type = type;
        this.displayName = displayName;
        this.isEmergency = isEmergency;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public boolean isEmergency() { return isEmergency; }
    public void setEmergency(boolean emergency) { isEmergency = emergency; }
}
