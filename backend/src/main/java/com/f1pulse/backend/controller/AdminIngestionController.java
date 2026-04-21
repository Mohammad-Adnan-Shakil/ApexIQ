package com.f1pulse.backend.controller;

import com.f1pulse.backend.service.HistoricalDataIngestionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin endpoints for managing F1 historical data ingestion
 * All endpoints require ADMIN role
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administration", description = "Admin endpoints for system management")
public class AdminIngestionController {

    @Autowired
    private HistoricalDataIngestionService ingestionService;

    /**
     * POST /api/admin/ingest-historical
     * Triggers full ingestion of all F1 historical data (1950-2026)
     */
    @PostMapping("/ingest-historical")
    public ResponseEntity<?> ingestAllHistoricalData() {
        log.info("🚀 Admin requested full historical data ingestion");
        
        try {
            // Run ingestion asynchronously
            new Thread(() -> {
                try {
                    ingestionService.ingestAllHistoricalData();
                    log.info("✅ Full ingestion completed successfully");
                } catch (Exception e) {
                    log.error("❌ Full ingestion failed", e);
                }
            }).start();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Full historical data ingestion started");
            response.put("status", "IN_PROGRESS");
            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            log.error("Error starting ingestion", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Failed to start ingestion", "details", e.getMessage())
            );
        }
    }

    /**
     * POST /api/admin/ingest-year/{year}
     * Triggers ingestion for a single season
     */
    @PostMapping("/ingest-year/{year}")
    public ResponseEntity<?> ingestSingleYear(@PathVariable Integer year) {
        log.info("🚀 Admin requested ingestion for year: {}", year);

        if (year < 1950 || year > 2026) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Year must be between 1950 and 2026")
            );
        }

        try {
            // Run ingestion asynchronously
            new Thread(() -> {
                try {
                    ingestionService.ingestSeason(year);
                    log.info("✅ Season {} ingestion completed successfully", year);
                } catch (Exception e) {
                    log.error("❌ Season {} ingestion failed", year, e);
                }
            }).start();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Season " + year + " ingestion started");
            response.put("status", "IN_PROGRESS");
            response.put("year", year.toString());
            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            log.error("Error starting single year ingestion", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Failed to start ingestion", "details", e.getMessage())
            );
        }
    }

    /**
     * GET /api/admin/ingestion-status
     * Returns current ingestion status
     */
    @GetMapping("/ingestion-status")
    public ResponseEntity<?> getIngestionStatus() {
        try {
            return ResponseEntity.ok(ingestionService.getIngestionStatus());
        } catch (Exception e) {
            log.error("Error retrieving ingestion status", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Failed to retrieve status", "details", e.getMessage())
            );
        }
    }
}
