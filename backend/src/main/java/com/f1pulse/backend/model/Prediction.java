package com.f1pulse.backend.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String race;
    private String driver;
    private int confidence;

    public Prediction() {
    }

    public Prediction(String race, String driver, int confidence) {
        this.race = race;
        this.driver = driver;
        this.confidence = confidence;
    }

    public Prediction(Long id, String race, String driver, int confidence) {
        this.id = id;
        this.race = race;
        this.driver = driver;
        this.confidence = confidence;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prediction that = (Prediction) o;
        return confidence == that.confidence &&
                Objects.equals(id, that.id) &&
                Objects.equals(race, that.race) &&
                Objects.equals(driver, that.driver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, race, driver, confidence);
    }

    @Override
    public String toString() {
        return "Prediction{" +
                "id=" + id +
                ", race='" + race + '\'' +
                ", driver='" + driver + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}
