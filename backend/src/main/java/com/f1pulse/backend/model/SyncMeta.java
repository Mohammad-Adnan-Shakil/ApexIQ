package com.f1pulse.backend.model;

import jakarta.persistence.*;

@Entity
public class SyncMeta {

    @Id
    private String id; // e.g. "drivers", "teams", "races"

    private long lastSyncTime;

    public SyncMeta() {}

    public SyncMeta(String id, long lastSyncTime) {
        this.id = id;
        this.lastSyncTime = lastSyncTime;
    }

    public String getId() { return id; }
    public long getLastSyncTime() { return lastSyncTime; }

    public void setId(String id) { this.id = id; }
    public void setLastSyncTime(long lastSyncTime) { this.lastSyncTime = lastSyncTime; }
}
