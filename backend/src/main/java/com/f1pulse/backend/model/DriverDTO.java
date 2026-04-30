package com.f1pulse.backend.model;

public class DriverDTO {

    private final String name;
    private final String code;
    private final String nationality;
    private final String team;
    private final Double points;

    public DriverDTO(String name, String code, String nationality, String team, Double points) {
        this.name = name;
        this.code = code;
        this.nationality = nationality;
        this.team = team;
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getNationality() {
        return nationality;
    }

    public String getTeam() {
        return team;
    }

    public Double getPoints() {
        return points;
    }
}
