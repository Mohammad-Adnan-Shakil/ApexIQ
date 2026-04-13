package com.f1pulse.backend.ai.controller;

import com.f1pulse.backend.ai.dto.DriverInsightsResponseDTO;
import com.f1pulse.backend.ai.service.DriverInsightsService;
import com.f1pulse.backend.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class DriverInsightsController {

    private final DriverInsightsService driverInsightsService;

    public DriverInsightsController(DriverInsightsService driverInsightsService) {
        this.driverInsightsService = driverInsightsService;
    }

    @GetMapping("/driver/{driverId}/insights")
    public ApiResponse<DriverInsightsResponseDTO> getInsights(@PathVariable Long driverId) {

        DriverInsightsResponseDTO insights =
                driverInsightsService.getDriverInsights(driverId);

        return new ApiResponse<>(
                true,
                "Driver insights generated",
                insights
        );
    }
}