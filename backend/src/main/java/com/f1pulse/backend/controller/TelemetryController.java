package com.deltabox.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/api/telemetry")
@Tag(name = "Telemetry Analysis", description = "FastF1 lap telemetry extraction and comparison")
public class TelemetryController {

    private final RestTemplate restTemplate;
    private final String mlServiceUrl;

    public TelemetryController(RestTemplate restTemplate, Environment environment) {
        this.restTemplate = restTemplate;
        this.mlServiceUrl = environment.getProperty("ML_SERVICE_URL", "http://localhost:8000");
    }

    @GetMapping("/compare")
    @Operation(summary = "Compare telemetry between two drivers",
            description = "Extracts and compares lap telemetry for two drivers from a specific F1 session. " +
                    "First run may take 20-30 seconds as FastF1 downloads session data.")
    @ApiResponse(responseCode = "200", description = "Telemetry data retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(example = "{\"driver1\":\"VER\",\"driver2\":\"LEC\"}")))
    @ApiResponse(responseCode = "500", description = "Failed to extract telemetry")
    public ResponseEntity<String> compareTelemetry(
            @RequestParam int year,
            @RequestParam String grandPrix,
            @RequestParam String sessionType,
            @RequestParam String driver1,
            @RequestParam String driver2) {

        log.info("📡 [TelemetryController] Telemetry request: {} {} {} {} vs {}", 
                year, grandPrix, sessionType, driver1, driver2);

        try {
            String url = mlServiceUrl + "/telemetry?year=" + year + 
                    "&grand_prix=" + grandPrix + 
                    "&session_type=" + sessionType + 
                    "&driver1=" + driver1.toUpperCase() + 
                    "&driver2=" + driver2.toUpperCase();

            log.info("📂 [TelemetryController] Calling ML service at: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                String.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("❌ [TelemetryController] ML service returned error: {}", response.getStatusCode());
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"ML service returned error: " + response.getStatusCode() + "\"}");
            }
            
            String body = response.getBody();
            if (body == null || body.isBlank()) {
                log.error("❌ [TelemetryController] ML service returned empty response");
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"ML service returned no data\"}");
            }

            log.info("✅ [TelemetryController] Successfully retrieved telemetry data");
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);

        } catch (Exception e) {
            log.error("❌ [TelemetryController] Error calling ML service: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Failed to call ML service: " + e.getMessage() + "\"}");
        }
    }
}
