package com.f1pulse.backend.ai.dto;

import jakarta.validation.constraints.*;

public class PredictionRequestDTO {

    @NotNull
    @Min(1)
    @Max(20)
    private Integer gridPosition;

    @NotNull
    @Min(0)
    @Max(10)
    private Integer driverForm;

    @NotNull
    @Min(0)
    @Max(10)
    private Integer teamPerformance;

    @NotNull
    @Min(0)
    @Max(10)
    private Integer trackAffinity;

    public Integer getGridPosition() {
        return gridPosition;
    }

    public void setGridPosition(Integer gridPosition) {
        this.gridPosition = gridPosition;
    }

    public Integer getDriverForm() {
        return driverForm;
    }

    public void setDriverForm(Integer driverForm) {
        this.driverForm = driverForm;
    }

    public Integer getTeamPerformance() {
        return teamPerformance;
    }

    public void setTeamPerformance(Integer teamPerformance) {
        this.teamPerformance = teamPerformance;
    }

    public Integer getTrackAffinity() {
        return trackAffinity;
    }

    public void setTrackAffinity(Integer trackAffinity) {
        this.trackAffinity = trackAffinity;
    }

    // getters & setters
}
