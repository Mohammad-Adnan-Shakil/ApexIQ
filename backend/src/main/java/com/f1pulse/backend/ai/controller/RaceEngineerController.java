package com.f1pulse.backend.ai.controller;

import com.f1pulse.backend.ai.dto.RaceContextRequest;
import com.f1pulse.backend.ai.service.RaceEngineerService;
import com.f1pulse.backend.exception.PythonExecutionException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for AI-powered race engineering advice.
 * Uses DeepSeek R1 for strategic decision making during F1 races.
 */
@RestController
@RequestMapping("/api/race-engineer")
@Tag(name = "Race Engineer", description = "AI-powered pit wall strategy and tactical advice using DeepSeek R1")
public class RaceEngineerController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RaceEngineerController.class);

    private final RaceEngineerService raceEngineerService;

    public RaceEngineerController(RaceEngineerService raceEngineerService) {
        this.raceEngineerService = raceEngineerService;
    }

    @PostMapping("/ask")
    @Operation(summary = "Get race engineering advice",
            description = "Request strategic advice from the AI race engineer based on current race context. " +
                    "Provides pit wall radio-style tactical recommendations.")
    @ApiResponse(responseCode = "200", description = "Engineering advice generated successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(example = "{\"response\":\"Box box, we'll switch to mediums at the next pit window.\"}")))
    @ApiResponse(responseCode = "503", description = "Race Engineer unavailable - check DeepSeek API connection")
    public ResponseEntity<Map<String, String>> getEngineerAdvice(
            @RequestBody RaceContextRequest raceContext) {

        log.info("📡 [RaceEngineerController] Received strategy request for P{} Lap {}", 
                raceContext.getPosition(), raceContext.getLap());

        try {
            // Build user message from race context
            String userMessage = String.format(
                "Lap %d of %d. Position: P%d. Gap to leader: %s. Tyre: %s (age %d laps). Fuel: %.1fkg. Weather: %s. Last lap: %s. Driver says: %s",
                raceContext.getLap(),
                raceContext.getTotalLaps(),
                raceContext.getPosition(),
                raceContext.getGapToLeader(),
                raceContext.getTyreCompound(),
                raceContext.getTyreAge(),
                raceContext.getFuelLoad(),
                raceContext.getWeather(),
                raceContext.getLastLapTime(),
                raceContext.getDriverMessage()
            );
            
            String advice = raceEngineerService.ask(userMessage, raceContext);
            
            Map<String, String> result = new HashMap<>();
            result.put("response", advice);
            log.info("✅ [RaceEngineerController] Generated advice successfully");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ [RaceEngineerController] Unexpected error: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate race strategy");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
