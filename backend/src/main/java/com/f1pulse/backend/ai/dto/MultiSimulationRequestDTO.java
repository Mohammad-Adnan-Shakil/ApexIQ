package com.f1pulse.backend.ai.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class MultiSimulationRequestDTO {

    @NotNull
    private Long driverId;

    @NotEmpty
    private List<Integer> simulatedPositions;

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public List<Integer> getSimulatedPositions() {
        return simulatedPositions;
    }

    public void setSimulatedPositions(List<Integer> simulatedPositions) {
        this.simulatedPositions = simulatedPositions;
    }
}
