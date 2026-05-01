package com.f1pulse.backend.ai.service;

import com.f1pulse.backend.ai.dto.PredictionRequestDTO;
import com.f1pulse.backend.ai.dto.PredictionResponseDTO;
import com.f1pulse.backend.service.MLClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of PredictionService that uses the ML microservice via HTTP.
 * Replaces legacy ProcessBuilder subprocess calls with REST API calls.
 */
@Service
public class PredictionServiceImpl implements PredictionService {

    private static final Logger log = LoggerFactory.getLogger(PredictionServiceImpl.class);

    private final MLClientService mlClientService;

    public PredictionServiceImpl(MLClientService mlClientService) {
        this.mlClientService = mlClientService;
    }

    @Override
    public PredictionResponseDTO predictRaceOutcome(PredictionRequestDTO request) {
        log.info("Predicting race outcome for grid position: {}", request.getGridPosition());

        // Build payload for ML service
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("grid_position", request.getGridPosition());
        payload.put("driver_form", request.getDriverForm());
        payload.put("team_performance", request.getTeamPerformance());
        payload.put("track_affinity", request.getTrackAffinity());
        
        // Add default values for required ML model fields
        payload.put("avg_last_5", 20.0 - (request.getDriverForm() * 1.5));  // Convert form to avg finish
        payload.put("std_last_5", Math.max(0.2, (10.0 - request.getDriverForm()) / 3.5));
        payload.put("avg_last_10", (20.0 - (request.getDriverForm() * 1.5) + request.getGridPosition()) / 2.0);
        payload.put("std_last_10", Math.max((10.0 - request.getDriverForm()) / 3.5, 
                                          Math.abs(request.getGridPosition() - (20.0 - request.getDriverForm() * 1.5)) / 4.0));
        payload.put("last_race_position", request.getGridPosition().doubleValue());
        payload.put("qualifying_position", request.getGridPosition());
        payload.put("constructor_id", "unknown");
        payload.put("track_id", "unknown");
        payload.put("season_year", 2026);
        payload.put("driver_id", "0");
        
        // Call ML service
        var mlResponse = mlClientService.predict(payload);

        // Map ML response to PredictionResponseDTO
        PredictionResponseDTO response = new PredictionResponseDTO();
        response.setPredictedPosition((int) Math.round((mlResponse.getRfPrediction() + mlResponse.getXgbPrediction()) / 2.0));
        response.setConfidence(mlResponse.getConfidence());
        response.setConfidenceLabel(mlResponse.getConfidenceLabel());
        response.setPredictedRange(mlResponse.getPredictedRange());
        response.setTrend(mlResponse.getTrend());
        response.setInsights(mlResponse.getInsights());
        response.setTopFeatures(mlResponse.getTopFeatures());
        
        log.info("Prediction complete: position={}, confidence={}", 
                 response.getPredictedPosition(), response.getConfidence());
        
        return response;
    }
}
