package com.f1pulse.backend.controller;

import com.f1pulse.backend.model.Driver;
import com.f1pulse.backend.repository.DriverRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@Tag(name = "Drivers", description = "F1 driver data and information")
public class DriverController {

    private static final Logger logger = LoggerFactory.getLogger(DriverController.class);
    private final DriverRepository driverRepository;

    public DriverController(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllDrivers() {
        logger.info("GET /api/drivers - Request received");
        try {
            // Use distinct query to prevent duplicates
            logger.info("Fetching drivers for season 2026");
            List<Driver> drivers = driverRepository.findDistinctBySeasonOrderByPointsDesc(2026);
            logger.info("Found {} drivers for season 2026", drivers.size());
            
            // Return empty list gracefully if no drivers found
            if (drivers.isEmpty()) {
                logger.info("No drivers found in database - returning empty list");
                return ResponseEntity.ok(drivers); // Return empty list instead of error
            }
            
            return ResponseEntity.ok(drivers);
        } catch (Exception e) {
            logger.error("Failed to load drivers", e);
            return ResponseEntity.status(500).body("Failed to load drivers: " + e.getMessage());
        }
    }
}
