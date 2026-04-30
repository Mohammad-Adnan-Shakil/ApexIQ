package com.f1pulse.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Historical constructor championship standings
 */
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Long getConstructorId() {
        return constructorId;
    }

    public void setConstructorId(Long constructorId) {
        this.constructorId = constructorId;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public BigDecimal getPoints() {
        return points;
    }

    public void setPoints(BigDecimal points) {
        this.points = points;
    }

    public Integer getWins() {
        return wins;
    }

    public void setWins(Integer wins) {
        this.wins = wins;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoricalConstructorStandings that = (HistoricalConstructorStandings) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(year, that.year) &&
                Objects.equals(constructorId, that.constructorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, year, constructorId);
    }

    @Override
    public String toString() {
        return "HistoricalConstructorStandings{" +
                "id=" + id +
                ", year=" + year +
                ", constructorId=" + constructorId +
                ", position=" + position +
                ", points=" + points +
                ", wins=" + wins +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
