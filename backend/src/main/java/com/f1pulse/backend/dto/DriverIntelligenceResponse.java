package com.f1pulse.backend.dto;

public class DriverIntelligenceResponse {

    private Long driverId;
    private double rfPrediction;
    private double xgbPrediction;
    private String simulationImpact;
    private String finalInsight;

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public double getRfPrediction() { return rfPrediction; }
    public void setRfPrediction(double rfPrediction) { this.rfPrediction = rfPrediction; }

    public double getXgbPrediction() { return xgbPrediction; }
    public void setXgbPrediction(double xgbPrediction) { this.xgbPrediction = xgbPrediction; }

    public String getSimulationImpact() { return simulationImpact; }
    public void setSimulationImpact(String simulationImpact) { this.simulationImpact = simulationImpact; }

    public String getFinalInsight() { return finalInsight; }
    public void setFinalInsight(String finalInsight) { this.finalInsight = finalInsight; }
}