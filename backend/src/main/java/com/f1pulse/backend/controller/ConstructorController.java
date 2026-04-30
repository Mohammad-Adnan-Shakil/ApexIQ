package com.f1pulse.backend.controller;

import com.f1pulse.backend.model.Team;
import com.f1pulse.backend.repository.TeamRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/constructors")
@Tag(name = "Teams", description = "F1 constructor/team data")
public class ConstructorController {

    private final TeamRepository teamRepository;

    public ConstructorController(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllConstructors() {
        try {
            List<Team> constructors = teamRepository.findAll();
            return ResponseEntity.ok(constructors);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to load constructors");
        }
    }
}
