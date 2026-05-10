package com.f1pulse.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.sql.Timestamp;

/**
 * Entity for telemetry cache table
 * Implements multi-layer caching strategy with session and driver context
 */
@Entity
@Table(name = "telemetry_cache")
public class TelemetryCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_key", nullable = false, length = 50)
    private String sessionKey;

    @Column(name = "driver_number", nullable = false)
    private Integer driverNumber;

    @Column(name = "meeting_key", nullable = false, length = 20)
    private String meetingKey;

    @Column(name = "telemetry_json", columnDefinition = "jsonb", nullable = false)
    private String telemetryJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "last_accessed", nullable = false, updatable = false)
    private Timestamp lastAccessed;

    // Constructors
    public TelemetryCache() {}

    public TelemetryCache(String sessionKey, Integer driverNumber, String meetingKey, String telemetryJson) {
        this.sessionKey = sessionKey;
        this.driverNumber = driverNumber;
        this.meetingKey = meetingKey;
        this.telemetryJson = telemetryJson;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public Integer getDriverNumber() {
        return driverNumber;
    }

    public String getMeetingKey() {
        return meetingKey;
    }

    public String getTelemetryJson() {
        return telemetryJson;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getLastAccessed() {
        return lastAccessed;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public void setDriverNumber(Integer driverNumber) {
        this.driverNumber = driverNumber;
    }

    public void setMeetingKey(String meetingKey) {
        this.meetingKey = meetingKey;
    }

    public void setTelemetryJson(String telemetryJson) {
        this.telemetryJson = telemetryJson;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastAccessed(Timestamp lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    @Override
    public String toString() {
        return "TelemetryCache{" +
                "id=" + id +
                ", sessionKey='" + sessionKey + '\'' +
                ", driverNumber=" + driverNumber +
                ", meetingKey='" + meetingKey + '\'' +
                ", telemetryJson='" + telemetryJson + '\'' +
                ", createdAt=" + createdAt +
                ", lastAccessed=" + lastAccessed + '\'' +
                '}';
    }
}
