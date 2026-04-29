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
    private List<Map<String, Object>> probabilityDistribution;
    private String trend;
    private List<String> uncertaintyFactors;
    private Map<String, Double> performanceBreakdown;

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

    public List<Map<String, Object>> getProbabilityDistribution() {
        return probabilityDistribution;
    }

    public String getTrend() {
        return trend;
    }

    public List<String> getUncertaintyFactors() {
        return uncertaintyFactors;
    }

    public Map<String, Double> getPerformanceBreakdown() {
        return performanceBreakdown;
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

    public void setProbabilityDistribution(List<Map<String, Object>> probabilityDistribution) {
        this.probabilityDistribution = probabilityDistribution;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public void setUncertaintyFactors(List<String> uncertaintyFactors) {
        this.uncertaintyFactors = uncertaintyFactors;
    }

    public void setPerformanceBreakdown(Map<String, Double> performanceBreakdown) {
        this.performanceBreakdown = performanceBreakdown;
    }
}
