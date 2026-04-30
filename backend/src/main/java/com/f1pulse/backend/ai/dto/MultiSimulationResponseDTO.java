package com.f1pulse.backend.ai.dto;

public class MultiSimulationResponseDTO {

    private double oldAverage;
    private double newAverage;

    private double consistencyChange;
    private double trendChange;

    private String impactLevel;
    private String projectedRankingImpact;

    public double getOldAverage() {
        return oldAverage;
    }

    public void setOldAverage(double oldAverage) {
        this.oldAverage = oldAverage;
    }

    public double getNewAverage() {
        return newAverage;
    }

    public void setNewAverage(double newAverage) {
        this.newAverage = newAverage;
    }

    public double getConsistencyChange() {
        return consistencyChange;
    }

    public void setConsistencyChange(double consistencyChange) {
        this.consistencyChange = consistencyChange;
    }

    public double getTrendChange() {
        return trendChange;
    }

    public void setTrendChange(double trendChange) {
        this.trendChange = trendChange;
    }

    public String getImpactLevel() {
        return impactLevel;
    }

    public void setImpactLevel(String impactLevel) {
        this.impactLevel = impactLevel;
    }

    public String getProjectedRankingImpact() {
        return projectedRankingImpact;
    }

    public void setProjectedRankingImpact(String projectedRankingImpact) {
        this.projectedRankingImpact = projectedRankingImpact;
    }
}
