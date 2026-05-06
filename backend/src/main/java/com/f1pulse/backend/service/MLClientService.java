package com.f1pulse.backend.service;

import com.f1pulse.backend.dto.DriverIntelligenceResponse;
import com.f1pulse.backend.dto.DriverComparisonResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ML Service client for communicating with the Python Flask ML microservice.
 * This service provides HTTP-based communication with the ML prediction service,
 * replacing the legacy ProcessBuilder subprocess approach.
 */
@Service
public class MLClientService {

    private static final Logger log = LoggerFactory.getLogger(MLClientService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String mlServiceUrl;

    public MLClientService(RestTemplate restTemplate, ObjectMapper objectMapper,
                           @Value("${ml.service.url}") String mlServiceUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.mlServiceUrl = normalizeBaseUrl(mlServiceUrl);
        log.info("ML Client Service configured at: {}", mlServiceUrl);
    }

    private String normalizeBaseUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalArgumentException("ML service URL is not configured");
        }

        String trimmedUrl = rawUrl.trim();
        if (trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://")) {
            return trimmedUrl;
        }

        return "http://" + trimmedUrl;
    }

    /**
     * Run ML prediction for a driver's race outcome.
     * Calls POST /predict on the ML service.
     */
    public DriverIntelligenceResponse predict(Map<String, Object> payload) {
        try {
            String url = mlServiceUrl + "/predict";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            log.info("Calling ML service at: {}", url);
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                JsonNode.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("ML service returned error: " + response.getStatusCode());
            }
            
            JsonNode body = response.getBody();
            if (body == null) {
                throw new RuntimeException("ML service returned empty response");
            }
            
            return mapToIntelligenceResponse(body);
            
        } catch (Exception e) {
            log.error("Error calling ML service predict endpoint", e);
            throw new RuntimeException("Failed to get ML prediction: " + e.getMessage(), e);
        }
    }

    /**
     * Get performance insights for a driver.
     * Calls POST /insights on the ML service.
     */
    public Map<String, Object> getInsights(Map<String, Object> payload) {
        try {
            String url = mlServiceUrl + "/insights";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            log.info("Calling ML service insights endpoint at: {}", url);
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("ML service returned error: " + response.getStatusCode());
            }
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error calling ML service insights endpoint", e);
            throw new RuntimeException("Failed to get insights: " + e.getMessage(), e);
        }
    }

    /**
     * Run what-if simulation with modified parameters.
     * Calls POST /simulate on the ML service.
     */
    public Map<String, Object> simulate(Map<String, Object> baseData, Map<String, Object> modifiers) {
        try {
            String url = mlServiceUrl + "/simulate";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("base_data", baseData);
            payload.put("modifiers", modifiers);
            payload.put("scenario_type", "what_if");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            log.info("Calling ML service simulate endpoint at: {}", url);
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("ML service returned error: " + response.getStatusCode());
            }
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error calling ML service simulate endpoint", e);
            throw new RuntimeException("Failed to run simulation: " + e.getMessage(), e);
        }
    }

    /**
     * Compare two drivers and calculate win probabilities.
     * Calls POST /compare on the ML service.
     */
    public DriverComparisonResponse compare(Map<String, Object> payload) {
        try {
            String url = mlServiceUrl + "/compare";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            log.info("Calling ML service compare endpoint at: {}", url);
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                JsonNode.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("ML service returned error: " + response.getStatusCode());
            }
            
            JsonNode json = response.getBody();
            return mapToComparisonResponse(json);
            
        } catch (Exception e) {
            log.error("Error calling ML service compare endpoint", e);
            throw new RuntimeException("Failed to get comparison from ML service: " + e.getMessage(), e);
        }
    }

    /**
     * Analyze telemetry for two drivers.
     * Calls GET /telemetry on the ML service.
     */
    public Map<String, Object> analyzeTelemetry(int year, String grandPrix, String sessionType, 
                                                  String driver1, String driver2) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(mlServiceUrl + "/telemetry")
                    .queryParam("year", year)
                    .queryParam("grand_prix", grandPrix)
                    .queryParam("session_type", sessionType)
                    .queryParam("driver1", driver1)
                    .queryParam("driver2", driver2)
                    .build()
                    .encode()
                    .toUriString();
            
            log.info("Calling ML service telemetry endpoint at: {}", url);
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                Map.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("ML service returned error: " + response.getStatusCode());
            }
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Error calling ML service telemetry endpoint", e);
            throw new RuntimeException("Failed to analyze telemetry: " + e.getMessage(), e);
        }
    }

    /**
     * Health check for the ML service.
     * Calls GET /health on the ML service.
     */
    public boolean isHealthy() {
        try {
            String url = mlServiceUrl + "/health";
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                return "healthy".equals(body.get("status"));
            }
            return false;
            
        } catch (Exception e) {
            log.warn("ML service health check failed: {}", e.getMessage());
            return false;
        }
    }

    private DriverIntelligenceResponse mapToIntelligenceResponse(JsonNode json) {
        DriverIntelligenceResponse response = new DriverIntelligenceResponse();
        
        response.setDriverId(json.path("driver_id").asLong());
        response.setRfPrediction(json.path("rf_prediction").asDouble());
        response.setXgbPrediction(json.path("xgb_prediction").asDouble());
        response.setConfidence(json.path("confidence").asDouble());
        response.setConfidenceLabel(json.path("confidence_label").asText());
        response.setSimulationImpact(json.path("simulation_impact").asText());
        response.setFinalInsight(json.path("final_insight").asText());
        response.setPredictedRange(json.has("predicted_range") ? json.path("predicted_range").asText() : null);
        response.setTrend(json.has("trend") ? json.path("trend").asText() : null);
        
        // Map uncertainty factors
        if (json.has("uncertainty_factors") && json.get("uncertainty_factors").isArray()) {
            List<String> uncertaintyFactors = new java.util.ArrayList<>();
            for (JsonNode factor : json.get("uncertainty_factors")) {
                uncertaintyFactors.add(factor.asText());
            }
            response.setUncertaintyFactors(uncertaintyFactors);
        }
        
        // Map probability distribution
        if (json.has("probability_distribution") && json.get("probability_distribution").isArray()) {
            List<Map<String, Object>> probDist = new java.util.ArrayList<>();
            for (JsonNode item : json.get("probability_distribution")) {
                Map<String, Object> distItem = new LinkedHashMap<>();
                distItem.put("position", item.path("position").asInt());
                distItem.put("probability", item.path("probability").asDouble());
                probDist.add(distItem);
            }
            response.setProbabilityDistribution(probDist);
        }
        
        // Map performance breakdown
        if (json.has("performance_breakdown") && json.get("performance_breakdown").isObject()) {
            JsonNode breakdown = json.get("performance_breakdown");
            Map<String, Double> perfBreakdown = new LinkedHashMap<>();
            breakdown.fields().forEachRemaining(entry -> {
                perfBreakdown.put(entry.getKey(), entry.getValue().asDouble());
            });
            response.setPerformanceBreakdown(perfBreakdown);
        }
        
        // Map applied weights
        if (json.has("applied_weights") && json.get("applied_weights").isObject()) {
            JsonNode weights = json.get("applied_weights");
            Map<String, Double> appliedWeights = new LinkedHashMap<>();
            weights.fields().forEachRemaining(entry -> {
                appliedWeights.put(entry.getKey(), entry.getValue().asDouble());
            });
            response.setAppliedWeights(appliedWeights);
        }
        
        // Map insights
        if (json.has("insights") && json.get("insights").isArray()) {
            List<String> insights = new java.util.ArrayList<>();
            for (JsonNode insight : json.get("insights")) {
                insights.add(insight.asText());
            }
            response.setInsights(insights);
        }
        
        // Map divergence
        if (json.has("divergence") && json.get("divergence").isObject()) {
            JsonNode divergence = json.get("divergence");
            Map<String, Object> divergenceMap = new LinkedHashMap<>();
            divergence.fields().forEachRemaining(entry -> {
                if (entry.getValue().isDouble() || entry.getValue().isInt()) {
                    divergenceMap.put(entry.getKey(), entry.getValue().asDouble());
                } else {
                    divergenceMap.put(entry.getKey(), entry.getValue().asText());
                }
            });
            response.setDivergence(divergenceMap);
        }
        
        // Map confidence reason
        if (json.has("confidence_reason") && json.get("confidence_reason").isTextual()) {
            response.setConfidenceReason(json.get("confidence_reason").asText());
        }
        
        // Map top features
        if (json.has("top_features") && json.get("top_features").isArray()) {
            List<Map<String, Object>> topFeatures = new java.util.ArrayList<>();
            for (JsonNode feature : json.get("top_features")) {
                Map<String, Object> featureMap = new LinkedHashMap<>();
                featureMap.put("feature", feature.path("feature").asText());
                featureMap.put("importance", feature.path("importance").asDouble());
                featureMap.put("explanation", feature.path("explanation").asText());
                topFeatures.add(featureMap);
            }
            response.setTopFeatures(topFeatures);
        }
        
        return response;
    }

    private DriverComparisonResponse mapToComparisonResponse(JsonNode json) {
        DriverComparisonResponse comparisonResponse = new DriverComparisonResponse();
        
        // Map driverA
        if (json.has("driverA") && json.get("driverA").isObject()) {
            JsonNode driverA = json.get("driverA");
            DriverComparisonResponse.DriverComparison driverAData = new DriverComparisonResponse.DriverComparison();
            driverAData.setName(driverA.path("name").asText());
            driverAData.setRange(driverA.path("range").asText());
            driverAData.setConfidence(driverA.path("confidence").asDouble());
            driverAData.setWinProbability(driverA.path("winProbability").asDouble());
            
            if (driverA.has("insights") && driverA.get("insights").isArray()) {
                List<String> insights = new java.util.ArrayList<>();
                for (JsonNode insight : driverA.get("insights")) {
                    insights.add(insight.asText());
                }
                driverAData.setInsights(insights);
            }
            comparisonResponse.setDriverA(driverAData);
        }
        
        // Map driverB
        if (json.has("driverB") && json.get("driverB").isObject()) {
            JsonNode driverB = json.get("driverB");
            DriverComparisonResponse.DriverComparison driverBData = new DriverComparisonResponse.DriverComparison();
            driverBData.setName(driverB.path("name").asText());
            driverBData.setRange(driverB.path("range").asText());
            driverBData.setConfidence(driverB.path("confidence").asDouble());
            driverBData.setWinProbability(driverB.path("winProbability").asDouble());
            
            if (driverB.has("insights") && driverB.get("insights").isArray()) {
                List<String> insights = new java.util.ArrayList<>();
                for (JsonNode insight : driverB.get("insights")) {
                    insights.add(insight.asText());
                }
                driverBData.setInsights(insights);
            }
            comparisonResponse.setDriverB(driverBData);
        }
        
        // Map winner
        if (json.has("winner") && json.get("winner").isTextual()) {
            comparisonResponse.setWinner(json.get("winner").asText());
        }
        
        // Map low confidence warning
        if (json.has("lowConfidenceWarning") && json.get("lowConfidenceWarning").isTextual()) {
            comparisonResponse.setLowConfidenceWarning(json.get("lowConfidenceWarning").asText());
        }
        
        return comparisonResponse;
    }
}
