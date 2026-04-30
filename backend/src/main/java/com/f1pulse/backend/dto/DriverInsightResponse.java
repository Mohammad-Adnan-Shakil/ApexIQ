package com.f1pulse.backend.dto;

public class DriverInsightResponse {

    private Long driverId;
    private double avgLast5;
    private double stdLast5;
    private double avgLast10;
    private double stdLast10;
    private double predictedNextPosition;
    private String insight;
    private double confidence;
    private String confidenceLabel;

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getConfidenceLabel() {
        return confidenceLabel;
    }

    public void setConfidenceLabel(String confidenceLabel) {
        this.confidenceLabel = confidenceLabel;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public double getAvgLast5() {
        return avgLast5;
    }

    public void setAvgLast5(double avgLast5) {
        this.avgLast5 = avgLast5;
    }

    public double getStdLast5() {
        return stdLast5;
    }

    public void setStdLast5(double stdLast5) {
        this.stdLast5 = stdLast5;
    }

    public double getAvgLast10() {
        return avgLast10;
    }

    public void setAvgLast10(double avgLast10) {
        this.avgLast10 = avgLast10;
    }

    public double getStdLast10() {
        return stdLast10;
    }

    public void setStdLast10(double stdLast10) {
        this.stdLast10 = stdLast10;
    }

    public double getPredictedNextPosition() {
        return predictedNextPosition;
    }

    public void setPredictedNextPosition(double predictedNextPosition) {
        this.predictedNextPosition = predictedNextPosition;
    }

    public String getInsight() {
        return insight;
    }

    public void setInsight(String insight) {
        this.insight = insight;
    }
}
