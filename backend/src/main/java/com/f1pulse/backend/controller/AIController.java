package com.deltabox.backend.controller;

import com.deltabox.backend.ai.dto.DriverInsightsResponseDTO;
import com.deltabox.backend.ai.dto.SimulationRequestDTO;
import com.deltabox.backend.ai.dto.SimulationResponseDTO;
import com.deltabox.backend.ai.service.DriverInsightsService;
import com.deltabox.backend.ai.service.SimulationService;
import com.deltabox.backend.dto.DriverIntelligenceResponse;
import com.deltabox.backend.service.AIService;
import com.f1pulse.backend.dto.DriverComparisonRequest;
import com.f1pulse.backend.dto.DriverComparisonResponse;
import com.f1pulse.backend.service.MLService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "DeltaBox Predictions", description = "AI-powered race predictions and driver intelligence")
public class AIController {

    private final AIService aiService;
    private final DriverInsightsService driverInsightsService;
    private final SimulationService simulationService;
    private final MLService mlService;

    public AIController(AIService aiService,
                        DriverInsightsService driverInsightsService,
                        SimulationService simulationService,
                        MLService mlService) {
        this.aiService = aiService;
        this.driverInsightsService = driverInsightsService;
        this.simulationService = simulationService;
        this.mlService = mlService;
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
            prediction.put("confidence", round2(intelligence.getConfidence()));
            prediction.put("predictedRange", intelligence.getPredictedRange());
            prediction.put("trend", intelligence.getTrend());
            prediction.put("uncertaintyFactors", intelligence.getUncertaintyFactors());
            prediction.put("probabilityDistribution", intelligence.getProbabilityDistribution());
            prediction.put("performanceBreakdown", intelligence.getPerformanceBreakdown());
            prediction.put("appliedWeights", intelligence.getAppliedWeights());
            prediction.put("insights", intelligence.getInsights());
            prediction.put("divergence", intelligence.getDivergence());
            prediction.put("confidenceReason", intelligence.getConfidenceReason());

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

    /**
     * GET /api/ai/model-metrics
     * Returns model performance metrics from ml/model_metrics.json
     */
    @GetMapping("/model-metrics")
    public ResponseEntity<?> getModelMetrics() {
        try {
            // Path to model metrics file
            String metricsPath = System.getProperty("user.dir") + "/ml/models/model_metrics.json";
            File metricsFile = new File(metricsPath);
            
            if (!metricsFile.exists()) {
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("error", "Model metrics file not found");
                error.put("message", "Please run the training script first: python ml/scripts/train_all_models_v3.py");
                error.put("expectedPath", metricsPath);
                return ResponseEntity.status(404).body(error);
            }
            
            // Read and parse JSON
            String jsonContent = new String(Files.readAllBytes(Paths.get(metricsPath)));
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> metrics = mapper.readValue(jsonContent, Map.class);
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Failed to load model metrics");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * POST /api/ai/compare
     * Compare two drivers and calculate win probabilities
     */
    @PostMapping("/compare")
    public ResponseEntity<?> compareDrivers(@RequestBody DriverComparisonRequest request) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("driverA_id", request.getDriverAId());
            payload.put("driverB_id", request.getDriverBId());
            payload.put("gridA", request.getGridA());
            payload.put("gridB", request.getGridB());
            payload.put("race_id", request.getRaceId());

            DriverComparisonResponse comparison = mlService.compare(payload);
            return ResponseEntity.ok(comparison);

        } catch (Exception e) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Driver comparison failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
