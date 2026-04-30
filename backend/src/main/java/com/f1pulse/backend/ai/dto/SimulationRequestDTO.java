package com.f1pulse.backend.ai.dto;

public class SimulationRequestDTO {

    private Long driverId;
    private int newPosition;

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public int getNewPosition() { return newPosition; }
    public void setNewPosition(int newPosition) { this.newPosition = newPosition; }
}
