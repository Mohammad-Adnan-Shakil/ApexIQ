package com.f1pulse.backend.controller;

import com.f1pulse.backend.model.Team;
import com.f1pulse.backend.service.F1Service;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/f1/teams")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Teams", description = "F1 team information")
public class TeamController {

    private final F1Service f1Service;

    public TeamController(F1Service f1Service) {
        this.f1Service = f1Service;
    }

    @GetMapping("/db")
    public List<Team> getTeamsFromDB() {
        return f1Service.getTeamsFromDB(); // we’ll add this next
    }

    @PostMapping("/save")
    public String saveTeams() {
        f1Service.saveTeams();
        return "Teams saved successfully";
    }
}