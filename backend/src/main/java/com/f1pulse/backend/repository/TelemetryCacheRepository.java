package com.f1pulse.backend.repository;

import com.f1pulse.backend.entity.TelemetryCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for telemetry cache operations
 * Supports multi-layer caching strategy with session and driver context
 */
@Repository
public interface TelemetryCacheRepository extends JpaRepository<TelemetryCache, Long> {

    /**
     * Find telemetry by session key and driver number
     * Uses composite unique key for efficient lookups
     */
    Optional<TelemetryCache> findBySessionKeyAndDriverNumber(String sessionKey, Integer driverNumber);

    /**
     * Find all cached telemetry for a session
     */
    List<TelemetryCache> findBySessionKey(String sessionKey);

    /**
     * Delete old cache entries (cleanup)
     */
    @Query("DELETE FROM TelemetryCache t WHERE t.createdAt < :cutoffDate")
    void deleteOldEntries(@Param("cutoffDate") String cutoffDate);

    /**
     * Find all entries for a specific meeting
     */
    List<TelemetryCache> findByMeetingKey(String meetingKey);

    /**
     * Find cache entries older than specified time
     */
    @Query("SELECT t FROM TelemetryCache t WHERE t.lastAccessed < :cutoffDate")
    List<TelemetryCache> findOldEntries(@Param("cutoffDate") String cutoffDate);
}
