package com.f1pulse.backend.dto;

public class PodiumDriverDTO {
    
    private Integer position;
    private String driverName;
    private String driverCode;
    private String country;
    private String team;
    private Integer points;

    public PodiumDriverDTO() {}

    public PodiumDriverDTO(Integer position, String driverName, String driverCode, String country, String team, Integer points) {
        this.position = position;
        this.driverName = driverName;
        this.driverCode = driverCode;
        this.country = country;
        this.team = team;
        this.points = points;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverCode() {
        return driverCode;
    }

    public void setDriverCode(String driverCode) {
        this.driverCode = driverCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}
