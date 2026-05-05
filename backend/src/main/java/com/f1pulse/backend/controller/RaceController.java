package com.f1pulse.backend.controller;

import com.f1pulse.backend.model.Race;
import com.f1pulse.backend.repository.RaceRepository;
import com.f1pulse.backend.dto.PodiumDriverDTO;
import com.f1pulse.backend.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/races")
@Tag(name = "Races", description = "F1 race schedule and results")
public class RaceController {

    private static final Logger logger = LoggerFactory.getLogger(RaceController.class);
    private final RaceRepository raceRepository;

    public RaceController(RaceRepository raceRepository) {
        this.raceRepository = raceRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllRaces() {
        logger.info("GET /api/races - Request received");
        try {
            logger.info("Fetching races for season 2026");
            List<Race> races = raceRepository.findBySeasonAndDriverIdIsNullOrderByDateAsc(2026);
            logger.info("Found {} races for season 2026", races.size());

            // Return empty list gracefully if no races found
            if (races.isEmpty()) {
                logger.info("No races found in database - returning empty list");
                return ResponseEntity.ok(races); // Return empty list instead of error
            }

            // Defensive dedupe: one schedule row per round.
            Map<String, Race> uniqueByRound = new LinkedHashMap<>();
            for (Race race : races) {
                String key = race.getRound() != null
                        ? "round-" + race.getRound()
                        : "fallback-" + race.getRaceName() + "-" + race.getDate();
                uniqueByRound.putIfAbsent(key, race);
            }

            List<Race> cleaned = uniqueByRound.values().stream()
                    .sorted(
                            Comparator.comparing((Race r) -> Objects.requireNonNullElse(r.getRound(), Integer.MAX_VALUE))
                                    .thenComparing(Race::getDate, Comparator.nullsLast(String::compareTo))
                    )
                    .peek(race -> race.setStatus(resolveRaceStatus(race.getDate())))
                    .toList();

            logger.info("Returning {} unique races after deduplication", cleaned.size());
            return ResponseEntity.ok(cleaned);
        } catch (Exception e) {
            logger.error("Failed to load races", e);
            return ResponseEntity.status(500).body("Failed to load races: " + e.getMessage());
        }
    }

    @GetMapping("/{raceId}/podium")
    public ResponseEntity<ApiResponse<List<PodiumDriverDTO>>> getRacePodium(@PathVariable Long raceId) {
        logger.info("GET /api/races/{}/podium - Request received", raceId);
        
        try {
            // Get all race results for this race
            Optional<Race> raceResult = raceRepository.findById(raceId);
            
            if (raceResult.isEmpty()) {
                logger.info("No results found for race ID: {}", raceId);
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "No results found for this race", new ArrayList<>())
                );
            }
            
            // Get all race results for this race (since we need list for filtering)
            List<Race> allRaceResults = raceRepository.findByRaceId(raceId);
            
            // Filter for completed races with positions 1-3
            List<PodiumDriverDTO> podium = allRaceResults.stream()
                .filter(race -> race.getPosition() != null && race.getPosition() >= 1 && race.getPosition() <= 3)
                .sorted(Comparator.comparing(Race::getPosition))
                .map(race -> new PodiumDriverDTO(
                    race.getPosition(),
                    generateDriverName(race.getDriverId()),
                    generateDriverCode(race.getDriverId()),
                    generateCountry(race.getDriverId()),
                    generateTeam(race.getDriverId()),
                    calculatePoints(race.getPosition())
                ))
                .limit(3)
                .toList();
            
            logger.info("Returning {} podium finishers for race ID: {}", podium.size(), raceId);
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Podium results retrieved successfully", podium)
            );
            
        } catch (Exception e) {
            logger.error("Failed to fetch podium for race ID: {}", raceId, e);
            return ResponseEntity.status(500).body(
                new ApiResponse<>(false, "Failed to fetch podium results", null)
            );
        }
    }
    
    // Helper methods for generating mock data (these would connect to actual driver data in production)
    private String generateDriverName(Long driverId) {
        // Mock driver names - in production this would query the Driver entity
        String[] names = {"Max Verstappen", "Lewis Hamilton", "Charles Leclerc", "Lando Norris", "Carlos Sainz"};
        return names[(int)(driverId % names.length)];
    }
    
    private String generateDriverCode(Long driverId) {
        // Mock driver codes - in production this would query the Driver entity
        String[] codes = {"VER", "HAM", "LEC", "NOR", "SAI"};
        return codes[(int)(driverId % codes.length)];
    }
    
    private String generateCountry(Long driverId) {
        // Mock countries - in production this would query the Driver entity
        String[] countries = {"Netherlands", "United Kingdom", "Monaco", "United Kingdom", "Spain"};
        return countries[(int)(driverId % countries.length)];
    }
    
    private String generateTeam(Long driverId) {
        // Mock teams - in production this would query the Driver entity
        String[] teams = {"Red Bull Racing", "Mercedes", "Ferrari", "McLaren", "Ferrari"};
        return teams[(int)(driverId % teams.length)];
    }
    
    private Integer calculatePoints(Integer position) {
        // F1 points system for 2026
        switch (position) {
            case 1: return 25;
            case 2: return 18;
            case 3: return 15;
            default: return 0;
        }
    }

    private static String resolveRaceStatus(String raceDate) {
        try {
            LocalDate parsed = LocalDate.parse(raceDate);
            return parsed.isAfter(LocalDate.now()) ? "SCHEDULED" : "COMPLETED";
        } catch (Exception ex) {
            return "SCHEDULED";
        }
    }
}
