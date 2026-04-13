package com.f1pulse.backend.ai.service;

import com.f1pulse.backend.ai.dto.DriverInsightsResponseDTO;
import com.f1pulse.backend.ai.dto.PredictionRequestDTO;
import com.f1pulse.backend.ai.dto.PredictionResponseDTO;

public interface PredictionService {

    PredictionResponseDTO predictRaceOutcome(PredictionRequestDTO request);
    DriverInsightsResponseDTO getDriverInsights(Long driverId);
}