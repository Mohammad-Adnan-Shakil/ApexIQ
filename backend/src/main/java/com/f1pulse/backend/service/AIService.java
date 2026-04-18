package com.f1pulse.backend.service;

import com.f1pulse.backend.dto.DriverIntelligenceResponse;
import com.f1pulse.backend.model.Race;
import com.f1pulse.backend.model.Driver;
import com.f1pulse.backend.model.Team;
import com.f1pulse.backend.repository.RaceRepository;
import com.f1pulse.backend.repository.DriverRepository;
import com.f1pulse.backend.repository.TeamRepository;
import com.f1pulse.backend.util.PythonExecutor;
import com.f1pulse.backend.util.StatsUtil;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AIService {

    private final RaceRepository raceRepository;
    private final DriverRepository driverRepository;
    private final TeamRepository teamRepository;

    public AIService(RaceRepository raceRepository,
                     DriverRepository driverRepository,
                     TeamRepository teamRepository) {
        this.raceRepository = raceRepository;
        this.driverRepository = driverRepository;
        this.teamRepository = teamRepository;
    }

    public DriverIntelligenceResponse getDriverIntelligence(Long driverId) {

        // 🔹 1. Fetch races
        List<Race> races = raceRepository.findTop10ByDriverIdOrderByDateDesc(driverId);

        if (races == null || races.isEmpty()) {
            throw new RuntimeException("No race data found for driver: " + driverId);
        }

        // 🔹 2. Stats
        double avgLast5 = StatsUtil.calculateAverage(races, 5);
        double stdLast5 = StatsUtil.calculateStdDev(races, 5);

        double avgLast10 = StatsUtil.calculateAverage(races, 10);
        double stdLast10 = StatsUtil.calculateStdDev(races, 10);

        Race latestRace = races.get(0);
        double lastRacePosition = latestRace.getPosition();

        // 🔹 3. REAL DATA EXTRACTION

        // Track
        String trackId = latestRace.getCircuitName()
                .toLowerCase()
                .replace(" ", "_");

        // Qualifying (fallback)
        int qualifyingPosition = latestRace.getPosition();

        // 🔥 FIXED CONSTRUCTOR FETCH
        String constructorId = "unknown";

        try {
            Optional<Driver> driverOpt = driverRepository.findById(driverId);

            if (driverOpt.isPresent()) {
                Driver driver = driverOpt.get();

                if (driver.getTeamId() != null) {
                    Optional<Team> teamOpt = teamRepository.findById(driver.getTeamId());

                    if (teamOpt.isPresent()) {
                        constructorId = teamOpt.get()
                                .getName()
                                .toLowerCase()
                                .replace(" ", "_");
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Constructor mapping fallback used");
        }

        // 🔹 4. Build JSON
        String jsonInput = String.format(
            "{"
            + "\"driver_id\":%d,"
            + "\"avg_last_5\":%.2f,"
            + "\"std_last_5\":%.2f,"
            + "\"avg_last_10\":%.2f,"
            + "\"std_last_10\":%.2f,"
            + "\"last_race_position\":%.2f,"
            + "\"qualifying_position\":%d,"
            + "\"constructor_id\":\"%s\","
            + "\"track_id\":\"%s\","
            + "\"season_year\":%d,"
            + "\"recent_avg_position_last_5\":%.2f,"
            + "\"recent_std_last_5\":%.2f,"
            + "\"grid_position\":%d,"
            + "\"is_home_race\":%d"
            + "}",

            driverId,
            avgLast5,
            stdLast5,
            avgLast10,
            stdLast10,
            lastRacePosition,
            qualifyingPosition,
            constructorId,
            trackId,
            2026,
            avgLast5,
            stdLast5,
            qualifyingPosition,
            0
        );

        // 🔹 5. Call Python
        String scriptPath = "ml/scripts/ai_orchestrator.py";
        JsonNode result = PythonExecutor.runScript(scriptPath, jsonInput);

        System.out.println("AI Orchestrator Output: " + result);

        if (result == null) {
            throw new RuntimeException("AI Orchestrator returned null response");
        }

        if (result.has("error")) {
            throw new RuntimeException("AI Orchestrator error: " + result.get("error").asText());
        }

        // 🔹 6. Map response
        DriverIntelligenceResponse res = new DriverIntelligenceResponse();
        res.setDriverId(driverId);

        res.setRfPrediction(result.path("rf_prediction").asDouble());
        res.setXgbPrediction(result.path("xgb_prediction").asDouble());
        res.setSimulationImpact(result.path("simulation_impact").asText());
        res.setFinalInsight(result.path("final_insight").asText());

        return res;
    }
}