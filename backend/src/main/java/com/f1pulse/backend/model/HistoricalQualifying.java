package com.deltabox.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Historical qualifying results from Ergast API
 */
@Data
@Entity
@Table(name = "historical_qualifying")
public class HistoricalQualifying {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "race_id", nullable = false)
    private Long raceId;

    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    @Column(name = "constructor_id")
    private Long constructorId;

    @Column(name = "position")
    private Integer position;

    @Column(name = "q1", length = 20)
    private String q1;

    @Column(name = "q2", length = 20)
    private String q2;

    @Column(name = "q3", length = 20)
    private String q3;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
