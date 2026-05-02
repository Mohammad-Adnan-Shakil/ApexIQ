package com.f1pulse.backend.ai.service;

import com.f1pulse.backend.ai.dto.RaceContextRequest;
import com.f1pulse.backend.exception.PythonExecutionException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AI-powered Race Engineer service using Groq API for strategic decision making.
 * Provides pit wall radio-style tactical recommendations based on race telemetry.
 */
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

    private static final long EXEC_TIMEOUT_SECONDS = 30L;

    public RaceEngineerService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        
        // Log API key presence on startup
        log.info("🔑 [RaceEngineer] GROQ_API_KEY present: {}", !apiKey.isEmpty());
        log.info("🌐 [RaceEngineer] Groq API URL: {}", apiUrl);
        log.info("🤖 [RaceEngineer] Groq model: {}", model);
    }

    /**
     * Generate race engineering strategic advice using Groq API.
     * 
     * @param context Race context including position, tires, fuel, and driver message
     * @return Map containing "response" key with engineer message value
     * @throws Exception on API communication or processing errors
     */
    public Map<String, String> getStrategicAdvice(RaceContextRequest context) {
        log.info("📡 [RaceEngineer] Processing context: Lap {}/{}, P{}, {}s gap, {} tires (age {})",
                context.getLap(), context.getTotalLaps(), context.getPosition(),
                context.getGapToLeader(), context.getTyreCompound(), context.getTyreAge());

        // Check if API key is available
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("⚠️ [RaceEngineer] GROQ_API_KEY not configured, returning fallback response");
            Map<String, String> fallback = new HashMap<>();
            fallback.put("response", "Race Engineer service is currently unavailable. Please check your tire wear and fuel load for strategic decisions.");
            return fallback;
        }

        try {
            // Build system prompt
            String systemPrompt = "You are an F1 race engineer on the pit wall. You make precise strategic decisions " +
                    "based on telemetry and race data. Speak in concise, professional pit wall radio style. Give one " +
                    "clear strategic recommendation. Maximum 4 sentences. Never break character. Never mention you are an AI.";

            // Build user prompt from race context
            String userPrompt = String.format(
                    "Lap %d of %d. Current position: P%d. Gap to leader: %s. Tyre: %s, age %d laps. " +
                    "Fuel: %.1fkg. Weather: %s. Last lap: %s. Driver says: %s",
                    context.getLap(),
                    context.getTotalLaps(),
                    context.getPosition(),
                    context.getGapToLeader(),
                    context.getTyreCompound(),
                    context.getTyreAge(),
                    context.getFuelLoad(),
                    context.getWeather(),
                    context.getLastLapTime(),
                    context.getDriverMessage()
            );

            log.debug("📝 [RaceEngineer] System: {}", systemPrompt);
            log.debug("📝 [RaceEngineer] User: {}", userPrompt);

            // Build DeepSeek API request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 200);

            List<Map<String, String>> messages = List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            );
            requestBody.put("messages", messages);

            log.info("🚀 [RaceEngineer] Sending request to Groq API: {}", apiUrl);

            // Call Groq API with timeout handling
            long startTime = System.currentTimeMillis();
            
            JsonNode response = callGroqApi(requestBody);
            
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("⏱️ [RaceEngineer] Groq response received in {}ms", elapsed);

            // Extract engineer message from response
            if (response == null || !response.has("choices") || response.get("choices").isEmpty()) {
                log.warn("⚠️ [RaceEngineer] Invalid response structure from API");
                throw new Exception("Invalid response from Groq API");
            }

            String engineerMessage = response
                    .get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();

            log.info("✅ [RaceEngineer] Generated advice: {}", engineerMessage);

            // Return response map
            Map<String, String> result = new HashMap<>();
            result.put("response", engineerMessage);
            return result;

        } catch (RestClientException e) {
            log.error("❌ [RaceEngineer] Groq API error: {}", e.getMessage(), e);
            Map<String, String> fallback = new HashMap<>();
            fallback.put("response", "Race Engineer service temporarily unavailable. Monitor tire temperatures and fuel consumption for optimal strategy.");
            return fallback;
        } catch (Exception e) {
            log.error("❌ [RaceEngineer] Unexpected error: {}", e.getMessage(), e);
            Map<String, String> fallback = new HashMap<>();
            fallback.put("response", "Race Engineer service experiencing technical difficulties. Use standard pit strategy guidelines.");
            return fallback;
        }
    }

    /**
     * Call Groq API with proper headers and error handling.
     */
    private JsonNode callGroqApi(Map<String, Object> requestBody) {
        try {
            log.info("🚀 [RaceEngineer] Calling Groq API at: {}", apiUrl);
            log.info("📝 [RaceEngineer] Request body: {}", objectMapper.writeValueAsString(requestBody));
            
            // Create HTTP headers with Bearer token
            var headers = new org.springframework.http.HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + apiKey);

            var request = new org.springframework.http.HttpEntity<>(requestBody, headers);

            // Execute POST request with timeout
            var response = restTemplate.postForEntity(apiUrl, request, String.class);
            
            log.info("📡 [RaceEngineer] API response status: {}", response.getStatusCode());
            log.info("📄 [RaceEngineer] API response body: {}", response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("❌ [RaceEngineer] API returned status: {}", response.getStatusCode());
                log.error("❌ [RaceEngineer] Full error response: {}", response.getBody());
                throw new Exception("Groq API error: " + response.getStatusCode());
            }

            // Parse response JSON
            return objectMapper.readTree(response.getBody());

        } catch (RestClientException e) {
            log.error("❌ [RaceEngineer] REST client error: {}", e.getMessage());
            log.error("❌ [RaceEngineer] Full exception: {}", e.toString());
            throw e;
        } catch (Exception e) {
            log.error("❌ [RaceEngineer] Failed to parse API response: {}", e.getMessage());
            log.error("❌ [RaceEngineer] Full exception: {}", e.toString());
            throw new RuntimeException("Failed to parse Groq response", e);
        }
    }
}
