package com.f1pulse.backend.ai.dto;

public class DriverInsightsResponseDTO {

    private double avgPosition;
    private int bestFinish;
    private int worstFinish;
    private double consistencyScore;
    private String formTrend;
    private String performanceRating;

    public DriverInsightsResponseDTO(double avgPosition, int bestFinish, int worstFinish,
            double consistencyScore, String formTrend, String performanceRating) {
        this.avgPosition = avgPosition;
        this.bestFinish = bestFinish;
        this.worstFinish = worstFinish;
        this.consistencyScore = consistencyScore;
        this.formTrend = formTrend;
        this.performanceRating = performanceRating;
    }

    public double getAvgPosition() {
        return avgPosition;
    }

    public int getBestFinish() {
        return bestFinish;
    }

    public int getWorstFinish() {
        return worstFinish;
    }

    public void setAvgPosition(double avgPosition) {
        this.avgPosition = avgPosition;
    }

    public void setBestFinish(int bestFinish) {
        this.bestFinish = bestFinish;
    }

    public void setWorstFinish(int worstFinish) {
        this.worstFinish = worstFinish;
    }

    public void setConsistencyScore(double consistencyScore) {
        this.consistencyScore = consistencyScore;
    }

    public void setFormTrend(String formTrend) {
        this.formTrend = formTrend;
    }

    public void setPerformanceRating(String performanceRating) {
        this.performanceRating = performanceRating;
    }

    public double getConsistencyScore() {
        return consistencyScore;
    }

    public String getFormTrend() {
        return formTrend;
    }

    public String getPerformanceRating() {
        return performanceRating;
    }
}
