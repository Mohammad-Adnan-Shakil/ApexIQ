package com.f1pulse.backend.ai.controller;

import com.f1pulse.backend.ai.dto.DriverInsightsResponseDTO;
import com.f1pulse.backend.ai.dto.PredictionRequestDTO;
import com.f1pulse.backend.ai.dto.PredictionResponseDTO;
import com.f1pulse.backend.ai.service.PredictionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.f1pulse.backend.dto.ApiResponse;;

@RestController
@RequestMapping("/api/ai")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping("/predict")
public ApiResponse<PredictionResponseDTO> predict(
        @Valid @RequestBody PredictionRequestDTO request
) {
    PredictionResponseDTO response = predictionService.predictRaceOutcome(request);

    return new ApiResponse<>(
            true,
            "Prediction successful",
            response
    );
}
@GetMapping("/driver/{driverId}/insights")
public ApiResponse<DriverInsightsResponseDTO> getDriverInsights(@PathVariable Long driverId) {

    DriverInsightsResponseDTO data = predictionService.getDriverInsights(driverId);

    return new ApiResponse<>(
            true,
            "Driver insights generated",
            data
    );
}
}