package com.f1pulse.backend.ai.dto;

public class DriverInsightsResponseDTO {

    private double avgPosition;
    private int bestFinish;
    private int worstFinish;
    private double consistencyScore;
    private String formTrend;

    // getters & setters

    public double getAvgPosition() { return avgPosition; }
    public void setAvgPosition(double avgPosition) { this.avgPosition = avgPosition; }

    public int getBestFinish() { return bestFinish; }
    public void setBestFinish(int bestFinish) { this.bestFinish = bestFinish; }

    public int getWorstFinish() { return worstFinish; }
    public void setWorstFinish(int worstFinish) { this.worstFinish = worstFinish; }

    public double getConsistencyScore() { return consistencyScore; }
    public void setConsistencyScore(double consistencyScore) { this.consistencyScore = consistencyScore; }

    public String getFormTrend() { return formTrend; }
    public void setFormTrend(String formTrend) { this.formTrend = formTrend; }
}