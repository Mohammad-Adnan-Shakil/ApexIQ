package com.f1pulse.backend.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "historical_race")
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoricalRace implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer seasonYear;

    private Integer round;
    private String raceName;
    private String circuitName;
    private String circuitCountry;

    private LocalDate raceDate;
    private String status; // COMPLETED, SCHEDULED, etc.

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date updatedAt;

    // ============= CONSTRUCTORS =============
    public HistoricalRace() {}

    public HistoricalRace(Integer seasonYear, Integer round, String raceName, 
                          String circuitName, String circuitCountry, LocalDate raceDate) {
        this.seasonYear = seasonYear;
        this.round = round;
        this.raceName = raceName;
        this.circuitName = circuitName;
        this.circuitCountry = circuitCountry;
        this.raceDate = raceDate;
        this.status = "COMPLETED";
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

    public Integer getSeasonYear() {
        return seasonYear;
    }

    public void setSeasonYear(Integer seasonYear) {
        this.seasonYear = seasonYear;
    }

    public Integer getRound() {
        return round;
    }

    public void setRound(Integer round) {
        this.round = round;
    }

    public String getRaceName() {
        return raceName;
    }

    public void setRaceName(String raceName) {
        this.raceName = raceName;
    }

    public String getCircuitName() {
        return circuitName;
    }

    public void setCircuitName(String circuitName) {
        this.circuitName = circuitName;
    }

    public String getCircuitCountry() {
        return circuitCountry;
    }

    public void setCircuitCountry(String circuitCountry) {
        this.circuitCountry = circuitCountry;
    }

    public LocalDate getRaceDate() {
        return raceDate;
    }

    public void setRaceDate(LocalDate raceDate) {
        this.raceDate = raceDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
