package com.f1pulse.backend.controller;

import com.f1pulse.backend.service.MLClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/telemetry")
@Tag(name = "Telemetry Analysis", description = "FastF1 lap telemetry extraction and comparison")
public class TelemetryController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TelemetryController.class);

    private final MLClientService mlClientService;

    public TelemetryController(MLClientService mlClientService) {
        this.mlClientService = mlClientService;
    }

    @GetMapping("/compare")
    @Operation(summary = "Compare telemetry between two drivers",
            description = "Returns FastF1 telemetry data for two drivers from a specific F1 session.")
    @ApiResponse(responseCode = "200", description = "Telemetry data retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(example = "{\"driver1\":\"VER\",\"driver2\":\"LEC\"}")))
    public ResponseEntity<Map<String, Object>> compareTelemetry(
            @RequestParam int year,
            @RequestParam String grandPrix,
            @RequestParam String sessionType,
            @RequestParam String driver1,
            @RequestParam String driver2) {

        // 🎯 LOG TELEMETRY REQUEST ENTRY
        log.info("🎯 TELEMETRY CONTROLLER: Incoming compareTelemetry request");
        log.info("  📋 Parameters: year={}, grandPrix='{}', sessionType='{}', driver1='{}', driver2='{}'", 
                year, grandPrix, sessionType, driver1, driver2);

        try {
            // 🚀 LOG ML SERVICE CALL
            log.info("🚀 Calling MLClientService.analyzeTelemetry()");
            
            Map<String, Object> telemetry = mlClientService.analyzeTelemetry(
                    year,
                    grandPrix,
                    sessionType,
                    driver1.toUpperCase(),
                    driver2.toUpperCase()
            );

            // ✅ LOG SUCCESS
            log.info("✅ Telemetry data retrieved from ML service - {} data points", 
                    ((java.util.List<?>) telemetry.getOrDefault("distance", java.util.List.of())).size());
            return ResponseEntity.ok(telemetry);
        } catch (Exception e) {
            // ❌ LOG ERROR
            log.error("❌ Telemetry service error: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Telemetry service temporarily unavailable.", "status", "error"));
        }
    }
}
