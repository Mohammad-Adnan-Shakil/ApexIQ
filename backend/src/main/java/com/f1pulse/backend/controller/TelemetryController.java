package com.f1pulse.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/telemetry")
@Tag(name = "Telemetry Analysis", description = "FastF1 lap telemetry extraction and comparison")
public class TelemetryController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TelemetryController.class);
    private final RestTemplate restTemplate;
    private final String mlServiceUrl;

    public TelemetryController(RestTemplate restTemplate, Environment environment) {
        this.restTemplate = restTemplate;
        this.mlServiceUrl = environment.getProperty("ML_SERVICE_URL", "http://localhost:8000");
    }

    @GetMapping("/compare")
    @Operation(summary = "Compare telemetry between two drivers",
            description = "Returns simulated telemetry data for two drivers from a specific F1 session. " +
                    "Provides realistic-looking telemetry data for visualization purposes.")
    @ApiResponse(responseCode = "200", description = "Telemetry data retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(example = "{\"driver1\":\"VER\",\"driver2\":\"LEC\"}")))
    public ResponseEntity<String> compareTelemetry(
            @RequestParam int year,
            @RequestParam String grandPrix,
            @RequestParam String sessionType,
            @RequestParam String driver1,
            @RequestParam String driver2) {

        log.info("📡 [TelemetryController] Telemetry request: {} {} {} {} vs {}", 
                year, grandPrix, sessionType, driver1, driver2);

        try {
            // Generate realistic mock telemetry data
            String mockTelemetry = generateMockTelemetry(driver1.toUpperCase(), driver2.toUpperCase(), year, grandPrix, sessionType);
            
            log.info("✅ [TelemetryController] Generated mock telemetry data");
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(mockTelemetry);

        } catch (Exception e) {
            log.error("❌ [TelemetryController] Error generating telemetry: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Telemetry generation failed. Please try again.\",\"status\":\"error\"}");
        }
    }
    
    private String generateMockTelemetry(String driver1, String driver2, int year, String grandPrix, String sessionType) {
        // Generate realistic-looking telemetry data matching frontend expectations
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        // Common distance array
        json.append("\"distance\":[");
        for (int i = 0; i < 100; i++) {
            json.append(i * 50.0);
            if (i < 99) json.append(",");
        }
        json.append("],");
        
        // Driver 1 telemetry arrays
        json.append("\"driver1_speed\":[");
        for (int i = 0; i < 100; i++) {
            json.append(String.format("%.1f", 250 + Math.sin(i * 0.1) * 50));
            if (i < 99) json.append(",");
        }
        json.append("],");
        
        json.append("\"driver1_throttle\":[");
        for (int i = 0; i < 100; i++) {
            json.append(String.format("%.2f", 0.8 + Math.random() * 0.2));
            if (i < 99) json.append(",");
        }
        json.append("],");
        
        json.append("\"driver1_brake\":[");
        for (int i = 0; i < 100; i++) {
            json.append(Math.random() < 0.1 ? "1" : "0");
            if (i < 99) json.append(",");
        }
        json.append("],");
        
        json.append("\"driver1_gear\":[");
        for (int i = 0; i < 100; i++) {
            json.append(1 + (int)(Math.random() * 7));
            if (i < 99) json.append(",");
        }
        json.append("],");
        
        // Driver 2 telemetry arrays
        json.append("\"driver2_speed\":[");
        for (int i = 0; i < 100; i++) {
            json.append(String.format("%.1f", 240 + Math.sin(i * 0.12) * 40));
            if (i < 99) json.append(",");
        }
        json.append("],");
        
        json.append("\"driver2_throttle\":[");
        for (int i = 0; i < 100; i++) {
            json.append(String.format("%.2f", 0.75 + Math.random() * 0.25));
            if (i < 99) json.append(",");
        }
        json.append("],");
        
        json.append("\"driver2_brake\":[");
        for (int i = 0; i < 100; i++) {
            json.append(Math.random() < 0.15 ? "1" : "0");
            if (i < 99) json.append(",");
        }
        json.append("],");
        
        json.append("\"driver2_gear\":[");
        for (int i = 0; i < 100; i++) {
            json.append(1 + (int)(Math.random() * 7));
            if (i < 99) json.append(",");
        }
        json.append("],");
        
        // Delta array
        json.append("\"delta\":[");
        for (int i = 0; i < 100; i++) {
            json.append(String.format("%.3f", (Math.random() - 0.5) * 2.0));
            if (i < 99) json.append(",");
        }
        json.append("],");
        
        // Lap times
        json.append("\"driver1_lap_time\":\"1:23.456\",");
        json.append("\"driver2_lap_time\":\"1:24.123\"");
        
        json.append("}");
        return json.toString();
    }
}
