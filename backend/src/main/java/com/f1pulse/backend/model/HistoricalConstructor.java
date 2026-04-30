package com.f1pulse.backend.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "historical_constructor")
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoricalConstructor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String constructorRef; // Ergast API reference

    private String name;
    private String nationality;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer totalWins = 0;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer totalChampionships = 0;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date updatedAt;

    // ============= CONSTRUCTORS =============
    public HistoricalConstructor() {}

    public HistoricalConstructor(String constructorRef, String name, String nationality) {
        this.constructorRef = constructorRef;
        this.name = name;
        this.nationality = nationality;
        this.totalWins = 0;
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

    public String getConstructorRef() {
        return constructorRef;
    }

    public void setConstructorRef(String constructorRef) {
        this.constructorRef = constructorRef;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public Integer getTotalWins() {
        return totalWins;
    }

    public void setTotalWins(Integer totalWins) {
        this.totalWins = totalWins;
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
