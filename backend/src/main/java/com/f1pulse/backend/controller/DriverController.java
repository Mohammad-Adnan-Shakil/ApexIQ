package com.f1pulse.backend.controller;

import com.f1pulse.backend.model.Driver;
import com.f1pulse.backend.repository.DriverRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@Tag(name = "Drivers", description = "F1 driver data and information")
public class DriverController {

    private final DriverRepository driverRepository;

    public DriverController(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllDrivers() {
        try {
            List<Driver> drivers = driverRepository.findBySeasonOrderByPointsDesc(2026);
            return ResponseEntity.ok(drivers);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to load drivers");
        }
    }
}
