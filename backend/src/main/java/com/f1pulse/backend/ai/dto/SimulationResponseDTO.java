package com.f1pulse.backend.ai.dto;

public class SimulationResponseDTO {

    private double oldAverage;
    private double newAverage;
    private String impact;

    public SimulationResponseDTO(double oldAverage, double newAverage, String impact) {
        this.oldAverage = oldAverage;
        this.newAverage = newAverage;
        this.impact = impact;
    }

    public double getOldAverage() { return oldAverage; }
    public double getNewAverage() { return newAverage; }
    public String getImpact() { return impact; }
}
