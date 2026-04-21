package com.f1pulse.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.f1pulse.backend.dto.DriverIntelligenceResponse;
import com.f1pulse.backend.model.Driver;
import com.f1pulse.backend.model.Race;
import com.f1pulse.backend.model.Team;
import com.f1pulse.backend.repository.DriverRepository;
import com.f1pulse.backend.repository.RaceRepository;
import com.f1pulse.backend.repository.TeamRepository;
import com.f1pulse.backend.util.PythonExecutor;
import com.f1pulse.backend.util.StatsUtil;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AIService {

    private final RaceRepository raceRepository;
    private final DriverRepository driverRepository;
    private final TeamRepository teamRepository;
    private final PythonExecutor pythonExecutor;

    public AIService(RaceRepository raceRepository,
                     DriverRepository driverRepository,
                     TeamRepository teamRepository,
                     PythonExecutor pythonExecutor) {
        this.raceRepository = raceRepository;
        this.driverRepository = driverRepository;
        this.teamRepository = teamRepository;
        this.pythonExecutor = pythonExecutor;
    }

    public DriverIntelligenceResponse getDriverIntelligence(Long driverId) {
        List<Race> races = raceRepository.findTop10ByDriverIdAndPositionIsNotNullOrderByDateDesc(driverId);

        if (races.isEmpty()) {
            throw new RuntimeException("No completed race data found for driver: " + driverId);
        }

        double avgLast5 = StatsUtil.calculateAverage(races, 5);
        double stdLast5 = StatsUtil.calculateStdDev(races, 5);
        double avgLast10 = StatsUtil.calculateAverage(races, 10);
        double stdLast10 = StatsUtil.calculateStdDev(races, 10);

        Race latestRace = races.getFirst();
        int qualifyingPosition = latestRace.getPosition();
        double lastRacePosition = latestRace.getPosition();

        String trackId = normalizeToken(latestRace.getCircuitName());
        String constructorId = resolveConstructorId(driverId);

        Map<String, Object> modelInput = new LinkedHashMap<>();
        modelInput.put("driver_id", driverId);
        modelInput.put("avg_last_5", round2(avgLast5));
        modelInput.put("std_last_5", round2(stdLast5));
        modelInput.put("avg_last_10", round2(avgLast10));
        modelInput.put("std_last_10", round2(stdLast10));
        modelInput.put("last_race_position", round2(lastRacePosition));
        modelInput.put("qualifying_position", qualifyingPosition);
        modelInput.put("constructor_id", constructorId);
        modelInput.put("track_id", trackId);
        modelInput.put("season_year", 2026);
        modelInput.put("recent_avg_position_last_5", round2(avgLast5));
        modelInput.put("recent_std_last_5", round2(stdLast5));
        modelInput.put("grid_position", qualifyingPosition);
        modelInput.put("is_home_race", 0);

        JsonNode result = pythonExecutor.runScript("ml/scripts/ai_orchestrator.py", modelInput);

        DriverIntelligenceResponse response = new DriverIntelligenceResponse();
        response.setDriverId(driverId);
        response.setRfPrediction(round2(result.path("rf_prediction").asDouble()));
        response.setXgbPrediction(round2(result.path("xgb_prediction").asDouble()));
        response.setSimulationImpact(result.path("simulation_impact").asText("neutral"));
        response.setFinalInsight(result.path("final_insight").asText("Model output generated."));
        response.setConfidence(round2(result.path("confidence").asDouble()));
        response.setConfidenceLabel(result.path("confidence_label").asText("unknown"));
        
        // ✅ Extract top features for "Why this prediction?" explanation
        try {
            if (result.has("top_features") && result.get("top_features").isArray()) {
                java.util.List<Map<String, Object>> topFeatures = new java.util.ArrayList<>();
                for (JsonNode feature : result.get("top_features")) {
                    Map<String, Object> featureMap = new java.util.HashMap<>();
                    featureMap.put("feature", feature.path("feature").asText());
                    featureMap.put("importance", feature.path("importance").asDouble());
                    featureMap.put("explanation", feature.path("explanation").asText());
                    topFeatures.add(featureMap);
                }
                response.setTopFeatures(topFeatures);
            }
        } catch (Exception e) {
            // Skip top features if not available
        }
        
        return response;
    }

    private String resolveConstructorId(Long driverId) {
        Optional<Driver> driverOpt = driverRepository.findById(driverId);
        if (driverOpt.isEmpty()) {
            return "unknown";
        }

        Driver driver = driverOpt.get();
        if (driver.getTeam() != null && !driver.getTeam().isBlank()) {
            return normalizeToken(driver.getTeam());
        }

        if (driver.getTeamId() != null) {
            Optional<Team> teamOpt = teamRepository.findById(driver.getTeamId());
            if (teamOpt.isPresent()) {
                return normalizeToken(teamOpt.get().getName());
            }
        }

        return "unknown";
    }

    private static String normalizeToken(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.toLowerCase().replace(" ", "_").replace('-', '_');
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
