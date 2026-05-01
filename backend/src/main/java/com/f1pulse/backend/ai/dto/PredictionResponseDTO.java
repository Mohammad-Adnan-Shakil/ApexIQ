package com.f1pulse.backend.ai.dto;

import java.util.List;
import java.util.Map;

/**
 * ✅ Production-Ready Prediction Response DTO
 * 
 * Contains:
 * - predictedPosition: Finishing position (1-20)
 * - confidence: Confidence level (0.0 - 1.0, where 1.0 = 100%)
 * - confidenceLabel: "low", "medium", or "high"
 * - predictedRange: e.g., "P1–P3", "P5–P10"
 * - trend: "IMPROVING", "DECLINING", or "STABLE"
 * - insights: List of insight strings
 * - topFeatures: List of feature importance maps
 */
public class PredictionResponseDTO {

    private double predictedPosition;
    
    // ✅ FIXED: Changed from String to double for proper calculation
    // Now frontend can correctly calculate: confidence * 100 = percentage
    private double confidence;
    
    private String confidenceLabel;
    private String predictedRange;
    private String trend;
    private List<String> insights;
    private List<Map<String, Object>> topFeatures;

    public PredictionResponseDTO() {}
    
    public PredictionResponseDTO(double predictedPosition, double confidence) {
        this.predictedPosition = predictedPosition;
        this.confidence = confidence;
    }

    public double getPredictedPosition() {
        return predictedPosition;
    }

    public void setPredictedPosition(double predictedPosition) {
        this.predictedPosition = predictedPosition;
    }

    // ✅ FIXED: Now returns double instead of String
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

    public String getPredictedRange() {
        return predictedRange;
    }

    public void setPredictedRange(String predictedRange) {
        this.predictedRange = predictedRange;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public List<String> getInsights() {
        return insights;
    }

    public void setInsights(List<String> insights) {
        this.insights = insights;
    }

    public List<Map<String, Object>> getTopFeatures() {
        return topFeatures;
    }

    public void setTopFeatures(List<Map<String, Object>> topFeatures) {
        this.topFeatures = topFeatures;
    }
}
