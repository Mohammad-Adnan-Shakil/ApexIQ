package com.f1pulse.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "race")
public class Race {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String raceName;
    private String circuitName;
    private String location;
    private String country;
    private String date;

    private Integer position;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    public Race() {}

    public Race(Driver driver,
                String raceName,
                String circuitName,
                String location,
                String country,
                String date,
                Integer position) {
        this.driver = driver;
        this.raceName = raceName;
        this.circuitName = circuitName;
        this.location = location;
        this.country = country;
        this.date = date;
        this.position = position;
    }

    public Integer getPosition() { return position; }
    public Driver getDriver() { return driver; }
}