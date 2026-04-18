package com.f1pulse.backend.service;

import com.f1pulse.backend.dto.DriverIntelligenceResponse;
import com.f1pulse.backend.model.Race;
import com.f1pulse.backend.repository.RaceRepository;
import com.f1pulse.backend.util.PythonExecutor;
import com.f1pulse.backend.util.StatsUtil;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIService {

    private final RaceRepository raceRepository;

    public AIService(RaceRepository raceRepository) {
        this.raceRepository = raceRepository;
    }

    public DriverIntelligenceResponse getDriverIntelligence(Long driverId) {

        // 🔹 1. Fetch recent races
        List<Race> races = raceRepository.findTop10ByDriverIdOrderByDateDesc(driverId);

        if (races == null || races.isEmpty()) {
            throw new RuntimeException("No race data found for driver: " + driverId);
        }

        double avgLast5 = StatsUtil.calculateAverage(races, 5);
        double stdLast5 = StatsUtil.calculateStdDev(races, 5);

        double avgLast10 = StatsUtil.calculateAverage(races, 10);
        double stdLast10 = StatsUtil.calculateStdDev(races, 10);

        double lastRacePosition = races.get(0).getPosition();

        // 🔹 2. Build input JSON
        String jsonInput = String.format(
            "{\"driver_id\":%d,\"avg_last_5\":%.2f,\"std_last_5\":%.2f,\"avg_last_10\":%.2f,\"std_last_10\":%.2f,\"last_race_position\":%.2f," +
            "\"qualifying_position\":2,\"constructor_id\":\"red_bull\",\"track_id\":\"monza\",\"season_year\":2026," +
            "\"recent_avg_position_last_5\":%.2f,\"recent_std_last_5\":%.2f,\"grid_position\":2,\"is_home_race\":0}",
            driverId, avgLast5, stdLast5, avgLast10, stdLast10, lastRacePosition,
            avgLast5, stdLast5
        );

        // 🔹 3. Call orchestrator
        String scriptPath = "ml/scripts/ai_orchestrator.py";

        JsonNode result = PythonExecutor.runScript(scriptPath, jsonInput);

        // 🔥 DEBUG
        System.out.println("AI Orchestrator Output: " + result);

        // 🔥 ERROR HANDLING
        if (result == null) {
            throw new RuntimeException("AI Orchestrator returned null response");
        }

        if (result.has("error")) {
            throw new RuntimeException("AI Orchestrator error: " + result.get("error").asText());
        }

        // 🔹 4. Safe Mapping
        DriverIntelligenceResponse res = new DriverIntelligenceResponse();
        res.setDriverId(driverId);

        res.setRfPrediction(result.path("rf_prediction").asDouble());
        res.setXgbPrediction(result.path("xgb_prediction").asDouble());
        res.setSimulationImpact(result.path("simulation_impact").asText());
        res.setFinalInsight(result.path("final_insight").asText());

        return res;
    }
}