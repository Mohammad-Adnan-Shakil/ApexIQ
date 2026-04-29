package com.deltabox.backend.service;

import com.deltabox.backend.dto.DriverIntelligenceResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MLService {

    private static final Logger log = LoggerFactory.getLogger(MLService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String mlServiceUrl;

    public MLService(RestTemplate restTemplate, ObjectMapper objectMapper, Environment environment) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.mlServiceUrl = environment.getProperty("ML_SERVICE_URL", "http://localhost:8000");
    }

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
            
            return mapToResponse(body);
            
        } catch (Exception e) {
            log.error("Error calling ML service", e);
            throw new RuntimeException("Failed to get ML prediction: " + e.getMessage(), e);
        }
    }

    private DriverIntelligenceResponse mapToResponse(JsonNode json) {
        DriverIntelligenceResponse response = new DriverIntelligenceResponse();
        
        response.setDriverId(json.path("driver_id").asLong());
        response.setRfPrediction(json.path("rf_prediction").asDouble());
        response.setXgbPrediction(json.path("xgb_prediction").asDouble());
        response.setConfidence(json.path("confidence").asDouble());
        response.setConfidenceLabel(json.path("confidence_label").asText());
        response.setSimulationImpact(json.path("simulation_impact").asText());
        response.setFinalInsight(json.path("final_insight").asText());
        
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
}
