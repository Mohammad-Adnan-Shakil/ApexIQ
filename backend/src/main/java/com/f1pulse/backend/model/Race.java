package com.f1pulse.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "race")
public class Race {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long driverId;
    private Integer round;

    private String raceName;
    private String circuitName;
    private String location;
    private String country;
    private String date;
    private Integer season = 2026;
    private String status = "SCHEDULED";

    private Integer position;

    public Race() {}

    public Race(Long driverId,
                String raceName,
                String circuitName,
                String location,
                String country,
                String date,
                Integer position) {
        this.driverId = driverId;
        this.raceName = raceName;
        this.circuitName = circuitName;
        this.location = location;
        this.country = country;
        this.date = date;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public Long getDriverId() {
        return driverId;
    }

    public Integer getRound() {
        return round;
    }

    public String getRaceName() {
        return raceName;
    }

    public String getCircuitName() {
        return circuitName;
    }

    public String getLocation() {
        return location;
    }

    public String getCountry() {
        return country;
    }

    public String getDate() {
        return date;
    }

    public Integer getPosition() {
        return position;
    }

    public Long getRaceId() {
        return id;
    }

    public String getName() {
        return raceName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public void setRound(Integer round) {
        this.round = round;
    }

    public void setRaceName(String raceName) {
        this.raceName = raceName;
    }

    public void setCircuitName(String circuitName) {
        this.circuitName = circuitName;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getSeason() {
        return season;
    }

    public void setSeason(Integer season) {
        this.season = season;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
