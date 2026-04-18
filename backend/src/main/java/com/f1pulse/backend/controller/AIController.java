package com.f1pulse.backend.controller;

import com.f1pulse.backend.dto.DriverIntelligenceResponse;
import com.f1pulse.backend.service.AIService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/driver-intelligence/{driverId}")
    public ResponseEntity<DriverIntelligenceResponse> getDriverIntelligence(
            @PathVariable Long driverId
    ) {
        return ResponseEntity.ok(aiService.getDriverIntelligence(driverId));
    }
}