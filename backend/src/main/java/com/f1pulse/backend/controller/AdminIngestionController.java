package com.f1pulse.backend.controller;

import com.f1pulse.backend.service.HistoricalDataIngestionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Admin endpoints for managing F1 historical data ingestion
 * All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@EnableAsync
@Tag(name = "Administration", description = "Admin endpoints for system management")
public class AdminIngestionController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminIngestionController.class);

    @Autowired
    private HistoricalDataIngestionService ingestionService;

    /**
     * POST /api/admin/ingest/historical
     * Triggers full ingestion of all F1 historical data (1950-2024)
     * Accepts optional ?fromYear=1950&toYear=2024 params
     */
    @PostMapping("/ingest/historical")
    public ResponseEntity<?> ingestAllHistoricalData(
            @RequestParam(required = false) Integer fromYear,
            @RequestParam(required = false) Integer toYear) {
        log.info("🚀 Admin requested full historical data ingestion");
        
        // Set default years if not provided
        int startYear = fromYear != null ? fromYear : 1950;
        int endYear = toYear != null ? toYear : 2024;
        
        if (startYear < 1950 || endYear > 2026 || startYear > endYear) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid year range. Must be 1950-2026 with fromYear <= toYear")
            );
        }
        
        try {
            // Generate unique job ID
            String jobId = UUID.randomUUID().toString();
            
            // Run ingestion asynchronously
            ingestionService.ingestAllHistoricalData(jobId, startYear, endYear);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Full historical data ingestion started");
            response.put("status", "IN_PROGRESS");
            response.put("jobId", jobId);
            response.put("fromYear", startYear);
            response.put("toYear", endYear);
            response.put("totalYears", endYear - startYear + 1);
            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            log.error("Error starting ingestion", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Failed to start ingestion", "details", e.getMessage())
            );
        }
    }

    /**
     * POST /api/admin/ingest/year/{year}
     * Triggers ingestion for a single season
     */
    @PostMapping("/ingest/year/{year}")
    public ResponseEntity<?> ingestSingleYear(@PathVariable Integer year) {
        log.info("🚀 Admin requested ingestion for year: {}", year);

        if (year < 1950 || year > 2026) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Year must be between 1950 and 2026")
            );
        }

        try {
            // Generate unique job ID
            String jobId = UUID.randomUUID().toString();
            
            // Run ingestion asynchronously
            ingestionService.ingestAllHistoricalData(jobId, year, year);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Season " + year + " ingestion started");
            response.put("status", "IN_PROGRESS");
            response.put("jobId", jobId);
            response.put("year", year);
            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            log.error("Error starting single year ingestion", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Failed to start ingestion", "details", e.getMessage())
            );
        }
    }

    /**
     * GET /api/admin/ingest/status
     * Returns current ingestion status
     */
    @GetMapping("/ingest/status")
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
    
    /**
     * GET /api/admin/ingest/progress/{jobId}
     * Returns progress for a specific async ingestion job
     */
    @GetMapping("/ingest/progress/{jobId}")
    public ResponseEntity<?> getIngestionProgress(@PathVariable String jobId) {
        try {
            return ResponseEntity.ok(ingestionService.getIngestionProgress(jobId));
        } catch (Exception e) {
            log.error("Error retrieving ingestion progress", e);
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Failed to retrieve progress", "details", e.getMessage())
            );
        }
    }
}
