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
public class RaceEngineerService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RaceEngineerService.class);

    @Value("${GROQ_API_KEY:}")
    private String apiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    @Value("${groq.model:llama3-70b-8192}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RaceEngineerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        // DO NOT touch apiKey here - it is null at construction time
    }

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("GROQ_API_KEY not set - Race Engineer will use fallback mode");
        } else {
            // Log masked API key for security (show first 5 chars)
            String maskedKey = apiKey.length() > 5 ? apiKey.substring(0, 5) + "..." : apiKey;
            log.info("Race Engineer initialized with Groq API key: {}", maskedKey);
        }
    }

    public String ask(String userMessage, Object raceContext) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("GROQ_API_KEY not configured - Race Engineer service unavailable");
            return "Race Engineer service temporarily unavailable. API key not configured.";
        }
        
        try {
            log.info("Race Engineer service processing request: {}", userMessage);
            
            // Build system prompt
            String systemPrompt = "You are an F1 race engineer on the pit wall. You make precise strategic decisions " +
                    "based on telemetry and race data. Speak in concise, professional pit wall radio style. Give one " +
                    "clear strategic recommendation. Maximum 4 sentences. Never break character. Never mention you are an AI.";

            // Build user prompt from race context
            String userPrompt = String.format(
                    "Driver message: %s. Race context: %s",
                    userMessage,
                    raceContext != null ? raceContext.toString() : "No context provided"
            );

            // Build Groq API request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 200);
            requestBody.put("temperature", 0.7);

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
                return "Race Engineer service temporarily unavailable. API returned error status.";
            }

            // Parse response and extract message
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            if (responseJson == null) {
                log.error("Null response from Groq API");
                return "Race Engineer service temporarily unavailable. Invalid API response.";
            }
            
            if (!responseJson.has("choices") || responseJson.get("choices").isEmpty()) {
                log.error("Invalid response structure from Groq API: {}", response.getBody());
                return "Race Engineer service temporarily unavailable. Invalid response format.";
            }

            JsonNode choice = responseJson.get("choices").get(0);
            if (!choice.has("message") || !choice.get("message").has("content")) {
                log.error("Missing message content in Groq API response: {}", response.getBody());
                return "Race Engineer service temporarily unavailable. Missing response content.";
            }

            String engineerMessage = choice.get("message").get("content").asText();
            
            if (engineerMessage == null || engineerMessage.trim().isEmpty()) {
                log.error("Empty message content from Groq API");
                return "Race Engineer service temporarily unavailable. Empty response received.";
            }

            log.info("Generated race engineer advice: {}", engineerMessage);
            return engineerMessage;

        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("Network error accessing Groq API: {}", e.getMessage());
            return "Race Engineer service temporarily unavailable. Network connection failed.";
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("HTTP client error calling Groq API: {} - {}", e.getStatusCode(), e.getMessage());
            return "Race Engineer service temporarily unavailable. API authentication failed.";
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("HTTP server error from Groq API: {} - {}", e.getStatusCode(), e.getMessage());
            return "Race Engineer service temporarily unavailable. API server error.";
        } catch (Exception e) {
            log.error("Unexpected error in Race Engineer service: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return "Race Engineer service temporarily unavailable. Unexpected error occurred.";
        }
    }
}
