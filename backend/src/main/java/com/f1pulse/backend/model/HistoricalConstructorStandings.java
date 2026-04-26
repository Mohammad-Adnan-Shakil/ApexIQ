package com.deltabox.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Historical constructor championship standings
 */
@Data
@Entity
@Table(name = "historical_constructor_standings")
public class HistoricalConstructorStandings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "constructor_id", nullable = false)
    private Long constructorId;

    @Column(name = "position")
    private Integer position;

    @Column(name = "points", precision = 8, scale = 2)
    private BigDecimal points;

    @Column(name = "wins")
    private Integer wins;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
