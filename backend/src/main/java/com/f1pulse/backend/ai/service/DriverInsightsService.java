package com.f1pulse.backend.ai.service;

import com.f1pulse.backend.ai.dto.DriverInsightsResponseDTO;

public interface DriverInsightsService {
    DriverInsightsResponseDTO getDriverInsights(Long driverId);
}