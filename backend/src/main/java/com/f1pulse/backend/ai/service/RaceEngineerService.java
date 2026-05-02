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
            log.info("Race Engineer initialized with Groq API");
        }
    }

    public String ask(String userMessage, Object raceContext) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "Race Engineer service temporarily unavailable. Monitor tire temperatures and fuel consumption for optimal strategy.";
        }
        try {
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

            List<Map<String, String>> messages = List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            );
            requestBody.put("messages", messages);

            // Create HTTP headers with Bearer token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Call Groq API
            var response = restTemplate.postForEntity(apiUrl, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Groq API returned status: {}", response.getStatusCode());
                throw new Exception("Groq API error: " + response.getStatusCode());
            }

            // Parse response and extract message
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            if (responseJson == null || !responseJson.has("choices") || responseJson.get("choices").isEmpty()) {
                log.warn("Invalid response structure from Groq API");
                throw new Exception("Invalid response from Groq API");
            }

            String engineerMessage = responseJson
                    .get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();

            log.info("Generated race engineer advice: {}", engineerMessage);
            return engineerMessage;

        } catch (Exception e) {
            log.error("Groq API call failed: {}", e.getMessage(), e);
            return "Race Engineer service temporarily unavailable. Monitor tire temperatures and fuel consumption for optimal strategy.";
        }
    }
}
