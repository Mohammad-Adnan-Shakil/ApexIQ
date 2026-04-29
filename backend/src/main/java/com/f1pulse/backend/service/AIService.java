package com.deltabox.backend.service;

import com.deltabox.backend.dto.DriverIntelligenceResponse;
import com.deltabox.backend.model.Driver;
import com.deltabox.backend.model.Race;
import com.deltabox.backend.model.Team;
import com.deltabox.backend.repository.DriverRepository;
import com.deltabox.backend.repository.RaceRepository;
import com.deltabox.backend.repository.TeamRepository;
import com.deltabox.backend.util.StatsUtil;
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
    private final MLService mlService;

    public AIService(RaceRepository raceRepository,
                     DriverRepository driverRepository,
                     TeamRepository teamRepository,
                     MLService mlService) {
        this.raceRepository = raceRepository;
        this.driverRepository = driverRepository;
        this.teamRepository = teamRepository;
        this.mlService = mlService;
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

        DriverIntelligenceResponse response = mlService.predict(modelInput);
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
