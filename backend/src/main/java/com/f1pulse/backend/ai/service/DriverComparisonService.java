package com.f1pulse.backend.ai.service;

import com.f1pulse.backend.ai.dto.DriverComparisonResponseDTO;

public interface DriverComparisonService {
    DriverComparisonResponseDTO compareDrivers(Long driver1, Long driver2);
}
