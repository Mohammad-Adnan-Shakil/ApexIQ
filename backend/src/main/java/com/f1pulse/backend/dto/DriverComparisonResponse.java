package com.f1pulse.backend.dto;

import java.util.List;
import java.util.Map;

public class DriverComparisonResponse {
    private DriverComparison driverA;
    private DriverComparison driverB;
    private String winner;
    private String lowConfidenceWarning;

    // GETTERS
    public DriverComparison getDriverA() {
        return driverA;
    }

    public DriverComparison getDriverB() {
        return driverB;
    }

    public String getWinner() {
        return winner;
    }

    public String getLowConfidenceWarning() {
        return lowConfidenceWarning;
    }

    // SETTERS
    public void setDriverA(DriverComparison driverA) {
        this.driverA = driverA;
    }

    public void setDriverB(DriverComparison driverB) {
        this.driverB = driverB;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public void setLowConfidenceWarning(String lowConfidenceWarning) {
        this.lowConfidenceWarning = lowConfidenceWarning;
    }

    // Inner class for individual driver comparison data
    public static class DriverComparison {
        private String name;
        private String range;
        private Double confidence;
        private Double winProbability;
        private List<String> insights;

        // GETTERS
        public String getName() {
            return name;
        }

        public String getRange() {
            return range;
        }

        public Double getConfidence() {
            return confidence;
        }

        public Double getWinProbability() {
            return winProbability;
        }

        public List<String> getInsights() {
            return insights;
        }

        // SETTERS
        public void setName(String name) {
            this.name = name;
        }

        public void setRange(String range) {
            this.range = range;
        }

        public void setConfidence(Double confidence) {
            this.confidence = confidence;
        }

        public void setWinProbability(Double winProbability) {
            this.winProbability = winProbability;
        }

        public void setInsights(List<String> insights) {
            this.insights = insights;
        }
    }
}
