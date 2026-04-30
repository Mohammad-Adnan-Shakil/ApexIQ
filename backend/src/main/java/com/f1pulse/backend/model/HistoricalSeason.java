package com.f1pulse.backend.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "historical_season")
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoricalSeason implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer year;

    private Integer totalRounds;

    private Long championDriverId;
    private Long championConstructorId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date updatedAt;

    // ============= CONSTRUCTORS =============
    public HistoricalSeason() {}

    public HistoricalSeason(Integer year, Integer totalRounds) {
        this.year = year;
        this.totalRounds = totalRounds;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // ============= GETTERS & SETTERS =============
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

    public Integer getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(Integer totalRounds) {
        this.totalRounds = totalRounds;
    }

    public Long getChampionDriverId() {
        return championDriverId;
    }

    public void setChampionDriverId(Long championDriverId) {
        this.championDriverId = championDriverId;
    }

    public Long getChampionConstructorId() {
        return championConstructorId;
    }

    public void setChampionConstructorId(Long championConstructorId) {
        this.championConstructorId = championConstructorId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
