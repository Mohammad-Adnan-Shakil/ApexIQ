package com.f1pulse.backend.model;

public class RaceDTO {

    private final Integer round;
    private final String raceName;
    private final String circuitName;
    private final String location;
    private final String country;
    private final String date;

    public RaceDTO(Integer round, String raceName, String circuitName, String location, String country, String date) {
        this.round = round;
        this.raceName = raceName;
        this.circuitName = circuitName;
        this.location = location;
        this.country = country;
        this.date = date;
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
}
