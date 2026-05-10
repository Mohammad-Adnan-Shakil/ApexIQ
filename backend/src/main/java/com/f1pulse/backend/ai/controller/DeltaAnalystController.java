package com.f1pulse.backend.ai.controller;

import com.f1pulse.backend.ai.dto.TelemetryAnalysisRequest;
import com.f1pulse.backend.ai.service.DeltaAnalystService;
import com.f1pulse.backend.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for Delta Analyst AI telemetry analysis.
 * Provides AI-powered insights and explanations for F1 telemetry data.
 * Uses Groq API for intelligent analysis with context-aware prompts.
 */
@RestController
@RequestMapping("/api/ai/delta-analyst")
@Tag(name = "Delta Analyst", description = "AI-powered F1 telemetry analysis and insights")
public class DeltaAnalystController {

    private static final Logger log = LoggerFactory.getLogger(DeltaAnalystController.class);

    @Autowired
    private DeltaAnalystService deltaAnalystService;

    /**
     * Analyze telemetry data with AI
     * Provides intelligent insights about F1 performance data
     */
    @Operation(
        summary = "Analyze telemetry data",
        description = "AI-powered analysis of F1 telemetry data with context-aware insights"
    )
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<String>> analyzeTelemetry(
            @RequestBody TelemetryAnalysisRequest request) {
        
        try {
            log.info("Received telemetry analysis request for drivers: {} and {}", 
                request.getDriver1Number(), request.getDriver2Number());

            String analysis = deltaAnalystService.analyzeTelemetry(request);
            
            log.info("Delta Analyst analysis completed successfully");
            return ResponseEntity.ok(new ApiResponse<>(true, "Analysis completed successfully", analysis));
            
        } catch (Exception e) {
            log.error("Failed to analyze telemetry: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to analyze telemetry: " + e.getMessage(), null));
        }
    }

    /**
     * Health check for Delta Analyst service
     */
    @Operation(
        summary = "Delta Analyst health check",
        description = "Check if Delta Analyst AI service is operational"
    )
    @PostMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "healthy");
            health.put("service", "Delta Analyst");
            health.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Delta Analyst is operational", health));
            
        } catch (Exception e) {
            log.error("Delta Analyst health check failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Delta Analyst health check failed", null));
        }
    }
}
