package com.deltabox.backend.dto;

import java.util.List;
import java.util.Map;

public class DriverIntelligenceResponse {

    private Long driverId;
    private double rfPrediction;
    private double xgbPrediction;
    private double confidence;
    private String confidenceLabel;
    private String simulationImpact;
    private String finalInsight;
    private List<Map<String, Object>> topFeatures;
    private String predictedRange;
    private Map<String, Double> probabilityDistribution;
    private String trend;

    // GETTERS

    public Long getDriverId() {
        return driverId;
    }

    public double getRfPrediction() {
        return rfPrediction;
    }

    public double getXgbPrediction() {
        return xgbPrediction;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getConfidenceLabel() {
        return confidenceLabel;
    }

    public String getSimulationImpact() {
        return simulationImpact;
    }

    public String getFinalInsight() {
        return finalInsight;
    }

    public List<Map<String, Object>> getTopFeatures() {
        return topFeatures;
    }

    public String getPredictedRange() {
        return predictedRange;
    }

    public Map<String, Double> getProbabilityDistribution() {
        return probabilityDistribution;
    }

    public String getTrend() {
        return trend;
    }

    // SETTERS

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public void setRfPrediction(double rfPrediction) {
        this.rfPrediction = rfPrediction;
    }

    public void setXgbPrediction(double xgbPrediction) {
        this.xgbPrediction = xgbPrediction;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public void setConfidenceLabel(String confidenceLabel) {
        this.confidenceLabel = confidenceLabel;
    }

    public void setSimulationImpact(String simulationImpact) {
        this.simulationImpact = simulationImpact;
    }

    public void setFinalInsight(String finalInsight) {
        this.finalInsight = finalInsight;
    }

    public void setTopFeatures(List<Map<String, Object>> topFeatures) {
        this.topFeatures = topFeatures;
    }

    public void setPredictedRange(String predictedRange) {
        this.predictedRange = predictedRange;
    }

    public void setProbabilityDistribution(Map<String, Double> probabilityDistribution) {
        this.probabilityDistribution = probabilityDistribution;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }
}
