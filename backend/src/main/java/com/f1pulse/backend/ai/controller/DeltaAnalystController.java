package com.f1pulse.backend.ai.controller;

import com.f1pulse.backend.ai.dto.DeltaAnalystChatRequest;
import com.f1pulse.backend.ai.service.DeltaAnalystService;
import com.f1pulse.backend.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for Delta Analyst AI telemetry analysis.
 * Provides AI-powered insights and explanations for F1 telemetry data.
 * Uses Groq API for intelligent analysis with context-aware prompts.
 */
@RestController
@RequestMapping("/api/ai")
@Tag(name = "Delta Analyst", description = "AI-powered F1 telemetry analysis and insights")
public class DeltaAnalystController {

    private final DeltaAnalystService deltaAnalystService;

    public DeltaAnalystController(DeltaAnalystService deltaAnalystService) {
        this.deltaAnalystService = deltaAnalystService;
    }

    /**
     * Analyze telemetry data with AI
     * Provides intelligent insights about F1 performance data
     */
    @Operation(
        summary = "Analyze telemetry data",
        description = "AI-powered analysis of F1 telemetry data with context-aware insights"
    )
    @PostMapping("/delta-analyst/chat")
    public ApiResponse<String> analyzeTelemetry(@RequestBody DeltaAnalystChatRequest request) {
        
        try {
            org.slf4j.LoggerFactory.getLogger(DeltaAnalystController.class)
                .info("Received telemetry analysis request: {}", request.getUserMessage());

            String analysis = deltaAnalystService.analyzeTelemetry(request);
            
            org.slf4j.LoggerFactory.getLogger(DeltaAnalystController.class)
                .info("Delta Analyst analysis completed successfully");
            return new ApiResponse<>(true, "Analysis completed successfully", analysis);
            
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(DeltaAnalystController.class)
                .error("Failed to analyze telemetry: {}", e.getMessage(), e);
            return new ApiResponse<>(false, "Failed to analyze telemetry: " + e.getMessage(), null);
        }
    }

    /**
     * Health check for Delta Analyst service
     */
    @Operation(
        summary = "Delta Analyst health check",
        description = "Check if Delta Analyst AI service is operational"
    )
    @PostMapping("/delta-analyst/health")
    public ApiResponse<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "healthy");
            health.put("service", "Delta Analyst");
            health.put("timestamp", System.currentTimeMillis());
            
            return new ApiResponse<>(true, "Delta Analyst is operational", health);
            
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(DeltaAnalystController.class)
                .error("Delta Analyst health check failed: {}", e.getMessage(), e);
            return new ApiResponse<>(false, "Delta Analyst health check failed", null);
        }
    }
}
