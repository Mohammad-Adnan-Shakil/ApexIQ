package com.f1pulse.backend.ai.dto;

/**
 * ✅ Production-Ready Prediction Response DTO
 * 
 * Contains:
 * - predictedPosition: Finishing position (1-20)
 * - confidence: Confidence level (0.0 - 1.0, where 1.0 = 100%)
 */
public class PredictionResponseDTO {

    private double predictedPosition;
    
    // ✅ FIXED: Changed from String to double for proper calculation
    // Now frontend can correctly calculate: confidence * 100 = percentage
    private double confidence;

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
}
