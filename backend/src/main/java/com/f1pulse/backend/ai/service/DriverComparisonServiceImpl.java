package com.f1pulse.backend.ai.service;

import com.f1pulse.backend.ai.dto.DriverComparisonResponseDTO;
import com.f1pulse.backend.ai.dto.DriverInsightsResponseDTO;
import org.springframework.stereotype.Service;

@Service
public class DriverComparisonServiceImpl implements DriverComparisonService {

    private final DriverInsightsService driverInsightsService;

    public DriverComparisonServiceImpl(DriverInsightsService driverInsightsService) {
        this.driverInsightsService = driverInsightsService;
    }

    @Override
    public DriverComparisonResponseDTO compareDrivers(Long driver1, Long driver2) {

        DriverInsightsResponseDTO d1 = driverInsightsService.getDriverInsights(driver1);
        DriverInsightsResponseDTO d2 = driverInsightsService.getDriverInsights(driver2);

        String better;
        String reason;

        if (d1.getAvgPosition() < d2.getAvgPosition()) {
            better = "DRIVER 1";
            reason = "Better average finishing position";
        } else if (d1.getAvgPosition() > d2.getAvgPosition()) {
            better = "DRIVER 2";
            reason = "Better average finishing position";
        } else {
            better = "EQUAL";
            reason = "Both drivers have similar performance";
        }

        return new DriverComparisonResponseDTO(better, reason);
    }
}
