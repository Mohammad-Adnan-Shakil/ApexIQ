package com.f1pulse.backend.dto;

public class DriverComparisonRequest {
    private Long driverAId;
    private Long driverBId;
    private Integer gridA;
    private Integer gridB;
    private Long raceId;

    // GETTERS
    public Long getDriverAId() {
        return driverAId;
    }

    public Long getDriverBId() {
        return driverBId;
    }

    public Integer getGridA() {
        return gridA;
    }

    public Integer getGridB() {
        return gridB;
    }

    public Long getRaceId() {
        return raceId;
    }

    // SETTERS
    public void setDriverAId(Long driverAId) {
        this.driverAId = driverAId;
    }

    public void setDriverBId(Long driverBId) {
        this.driverBId = driverBId;
    }

    public void setGridA(Integer gridA) {
        this.gridA = gridA;
    }

    public void setGridB(Integer gridB) {
        this.gridB = gridB;
    }

    public void setRaceId(Long raceId) {
        this.raceId = raceId;
    }
}
