package com.f1pulse.backend.controller;

import com.f1pulse.backend.ai.dto.DriverInsightsResponseDTO;
import com.f1pulse.backend.ai.dto.SimulationRequestDTO;
import com.f1pulse.backend.ai.dto.SimulationResponseDTO;
import com.f1pulse.backend.ai.service.DriverInsightsService;
import com.f1pulse.backend.ai.service.SimulationService;
import com.f1pulse.backend.dto.DriverIntelligenceResponse;
import com.f1pulse.backend.service.AIService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "ApexIQ Predictions", description = "AI-powered race predictions and driver intelligence")
public class AIController {

    private final AIService aiService;
    private final DriverInsightsService driverInsightsService;
    private final SimulationService simulationService;

    public AIController(AIService aiService,
                        DriverInsightsService driverInsightsService,
                        SimulationService simulationService) {
        this.aiService = aiService;
        this.driverInsightsService = driverInsightsService;
        this.simulationService = simulationService;
    }

    @GetMapping("/driver-intelligence/{driverId}")
    public ResponseEntity<DriverIntelligenceResponse> getDriverIntelligence(@PathVariable Long driverId) {
        return ResponseEntity.ok(aiService.getDriverIntelligence(driverId));
    }

    @PostMapping("/intelligence")
    public ResponseEntity<?> runAIPrediction(@RequestBody Map<String, Object> request) {
        try {
            Long driverId = ((Number) request.get("driverId")).longValue();
            Integer simulatedPosition = ((Number) request.get("simulatedPosition")).intValue();

            DriverIntelligenceResponse intelligence = aiService.getDriverIntelligence(driverId);
            DriverInsightsResponseDTO insights = driverInsightsService.getDriverInsights(driverId);

            SimulationRequestDTO simulationRequest = new SimulationRequestDTO();
            simulationRequest.setDriverId(driverId);
            simulationRequest.setNewPosition(simulatedPosition);
            SimulationResponseDTO simulation = simulationService.simulate(simulationRequest);

            Map<String, Object> prediction = new LinkedHashMap<>();
            prediction.put("predictedPosition", round2(intelligence.getXgbPrediction()));
            prediction.put("confidence", round2(intelligence.getConfidence()));

            Map<String, Object> insightPayload = new LinkedHashMap<>();
            insightPayload.put("averageFinish", round2(insights.getAvgPosition()));
            insightPayload.put("consistencyScore", round2(Math.min(1.0, insights.getConsistencyScore() / 10.0)));
            insightPayload.put("trend", insights.getFormTrend());
            insightPayload.put("rating", insights.getPerformanceRating());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("prediction", prediction);
            response.put("insights", insightPayload);
            response.put("simulation", simulation);
            response.put("summary", intelligence.getFinalInsight());
            response.put("confidenceLabel", intelligence.getConfidenceLabel());
            response.put("simulationImpact", intelligence.getSimulationImpact());
            
            // ✅ Add top features for "Why this prediction?" section
            if (intelligence.getTopFeatures() != null && !intelligence.getTopFeatures().isEmpty()) {
                response.put("topFeatures", intelligence.getTopFeatures());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "AI prediction failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
