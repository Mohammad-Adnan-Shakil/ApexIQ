package com.f1pulse.backend.ai.service;

import com.f1pulse.backend.ai.dto.DeltaAnalystChatRequest;
import com.f1pulse.backend.ai.prompts.DeltaAnalystPrompts;
import org.springframework.stereotype.Service;

/**
 * Delta Analyst AI service for telemetry analysis
 * Provides intelligent insights about F1 telemetry data using Groq API
 */
@Service
public class DeltaAnalystService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeltaAnalystService.class);

    private final GroqApiService groqApiService;

    public DeltaAnalystService(GroqApiService groqApiService) {
        this.groqApiService = groqApiService;
    }

    /**
     * Analyze telemetry data with AI
     * @param request DeltaAnalystChatRequest containing telemetry data and user question
     * @return AI analysis response
     */
    public String analyzeTelemetry(DeltaAnalystChatRequest request) {
        try {
            log.info("Delta Analyst service processing request: {}", request.getUserMessage());
            
            // Build prompts using the Delta Analyst prompts utility
            String systemPrompt = DeltaAnalystPrompts.DELTA_ANALYST_SYSTEM_PROMPT;
            String userPrompt = DeltaAnalystPrompts.buildUserPrompt(request.getUserMessage(), buildTelemetryContext(request));
            
            // Make request using the shared Groq API service
            return groqApiService.makeRequest(systemPrompt, userPrompt, 200, 0.7);
            
        } catch (Exception e) {
            log.error("Unexpected error in Delta Analyst service: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return "Delta Analyst service temporarily unavailable. Unexpected error occurred.";
        }
    }

    /**
     * Build telemetry context string from request data
     */
    private String buildTelemetryContext(DeltaAnalystChatRequest request) {
        StringBuilder context = new StringBuilder();
        
        context.append("TELEMETRY ANALYSIS REQUEST:\n");
        context.append(String.format("Drivers: %s vs %s\n", request.getDriver1(), request.getDriver2()));
        
        if (request.getRace() != null) {
            context.append(String.format("Race: %s", request.getRace()));
            if (request.getYear() != null) {
                context.append(String.format(" (%d)", request.getYear()));
            }
            context.append("\n");
        }
        
        if (request.getSession() != null) {
            context.append(String.format("Session: %s\n", request.getSession()));
        }
        
        context.append("\nTELEMETRY DATA AVAILABLE:\n");
        
        if (request.getDriver1Speed() != null || request.getDriver2Speed() != null) {
            context.append("- Speed data: Available for comparison\n");
        }
        
        if (request.getDriver1Throttle() != null || request.getDriver2Throttle() != null) {
            context.append("- Throttle data: Available for comparison\n");
        }
        
        if (request.getDriver1Brake() != null || request.getDriver2Brake() != null) {
            context.append("- Brake data: Available for comparison\n");
        }
        
        if (request.getGearData() != null) {
            context.append("- Gear data: Available\n");
        }
        
        if (request.getLapDelta() != null) {
            context.append("- Lap delta: Available\n");
        }
        
        return context.toString();
    }
}
