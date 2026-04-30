package com.f1pulse.backend.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "historical_driver")
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoricalDriver implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String driverRef; // Ergast API reference

    private String fullName;
    private String code;
    private String nationality;
    private LocalDate dateOfBirth;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer totalWins = 0;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer totalPoles = 0;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer totalChampionships = 0;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date updatedAt;

    // ============= CONSTRUCTORS =============
    public HistoricalDriver() {}

    public HistoricalDriver(String driverRef, String fullName, String code, String nationality) {
        this.driverRef = driverRef;
        this.fullName = fullName;
        this.code = code;
        this.nationality = nationality;
        this.totalWins = 0;
        this.totalPoles = 0;
        this.totalChampionships = 0;
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

    public String getDriverRef() {
        return driverRef;
    }

    public void setDriverRef(String driverRef) {
        this.driverRef = driverRef;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getTotalWins() {
        return totalWins;
    }

    public void setTotalWins(Integer totalWins) {
        this.totalWins = totalWins;
    }

    public Integer getTotalPoles() {
        return totalPoles;
    }

    public void setTotalPoles(Integer totalPoles) {
        this.totalPoles = totalPoles;
    }

    public Integer getTotalChampionships() {
        return totalChampionships;
    }

    public void setTotalChampionships(Integer totalChampionships) {
        this.totalChampionships = totalChampionships;
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
