package com.f1pulse.backend.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeltaAnalystService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeltaAnalystService.class);

    @Value("${GROQ_API_KEY:}")
    private String apiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    @Value("${groq.model:llama3-70b-8192}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DeltaAnalystService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        // DO NOT touch apiKey here - it is null at construction time
    }

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("GROQ_API_KEY not set - Delta Analyst will use fallback mode");
        } else {
            // Log masked API key for security (show first 5 chars)
            String maskedKey = apiKey.length() > 5 ? apiKey.substring(0, 5) + "..." : apiKey;
            log.info("Delta Analyst initialized with Groq API key: {}", maskedKey);
        }
    }

    public String analyzeTelemetry(String userQuestion, Object telemetryContext) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("GROQ_API_KEY not configured - Delta Analyst service unavailable");
            return "Delta Analyst temporarily unavailable.";
        }
        
        try {
            log.info("Delta Analyst service processing telemetry analysis: {}", userQuestion);
            
            // Build system prompt for telemetry analysis
            String systemPrompt = "You are Delta Analyst, an elite Formula 1 telemetry intelligence system inside DeltaBox.\n\n" +
                    "Your job is to analyze racing telemetry and explain performance differences between drivers.\n\n" +
                    "You analyze:\n" +
                    "- Speed traces\n" +
                    "- Throttle application\n" +
                    "- Brake pressure\n" +
                    "- Gear changes\n" +
                    "- Sector deltas\n" +
                    "- Corner entry and exit performance\n" +
                    "- Consistency over a lap\n\n" +
                    "Rules:\n" +
                    "- Speak like a professional race engineer/data analyst.\n" +
                    "- Be concise but insightful.\n" +
                    "- Always reference telemetry evidence.\n" +
                    "- Never give generic answers.\n" +
                    "- Compare drivers directly when asked.\n" +
                    "- Explain where lap time is gained or lost.";

            // Build user prompt from telemetry context
            String userPrompt = String.format(
                    "User question: %s. Telemetry context: %s",
                    userQuestion,
                    telemetryContext != null ? telemetryContext.toString() : "No telemetry data provided"
            );

            // Build Groq API request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 300);
            requestBody.put("temperature", 0.3);

            List<Map<String, String>> messages = List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            );
            requestBody.put("messages", messages);

            // Create HTTP headers with Bearer token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            // Trim whitespace/newlines from API key and ensure proper format
            String trimmedApiKey = apiKey.trim();
            String authorizationHeader = "Bearer " + trimmedApiKey;
            headers.set("Authorization", authorizationHeader);
            
            log.debug("Using API URL: {}", apiUrl);
            log.debug("Using model: {}", model);
            log.debug("Authorization header format: Bearer {}", trimmedApiKey.substring(0, Math.min(5, trimmedApiKey.length())) + "...");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Call Groq API with timeout handling
            log.debug("Calling Groq API at: {}", apiUrl);
            var response = restTemplate.postForEntity(apiUrl, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Groq API returned status: {} - Response: {}", response.getStatusCode(), response.getBody());
                return "Delta Analyst temporarily unavailable.";
            }

            // Parse response and extract message
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            if (responseJson == null) {
                log.error("Null response from Groq API");
                return "Delta Analyst temporarily unavailable.";
            }
            
            if (!responseJson.has("choices") || responseJson.get("choices").isEmpty()) {
                log.error("Invalid response structure from Groq API: {}", response.getBody());
                return "Delta Analyst temporarily unavailable.";
            }

            JsonNode choice = responseJson.get("choices").get(0);
            if (!choice.has("message") || !choice.get("message").has("content")) {
                log.error("Missing message content in Groq API response: {}", response.getBody());
                return "Delta Analyst temporarily unavailable.";
            }

            String analystMessage = choice.get("message").get("content").asText();
            
            if (analystMessage == null || analystMessage.trim().isEmpty()) {
                log.error("Empty message content from Groq API");
                return "Delta Analyst temporarily unavailable.";
            }

            log.info("Generated telemetry analysis: {}", analystMessage);
            return analystMessage;

        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("Network error accessing Groq API: {}", e.getMessage());
            return "Delta Analyst temporarily unavailable.";
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("HTTP client error calling Groq API: {} - {}", e.getStatusCode(), e.getMessage());
            return "Delta Analyst temporarily unavailable.";
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("HTTP server error from Groq API: {} - {}", e.getStatusCode(), e.getMessage());
            return "Delta Analyst temporarily unavailable.";
        } catch (Exception e) {
            log.error("Unexpected error in Delta Analyst service: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return "Delta Analyst temporarily unavailable.";
        }
    }
}
