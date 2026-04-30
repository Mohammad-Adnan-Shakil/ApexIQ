package com.f1pulse.backend.controller;

import com.f1pulse.backend.model.*;
import com.f1pulse.backend.repository.*;
import com.f1pulse.backend.service.ErgastService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Public endpoints for F1 historical data (1950-2026)
 * No authentication required - accessible to all users
 * 
 * Data sources:
 * 1. Ergast API (https://api.jolpi.ca/ergast/) - primary source with caching
 * 2. Local PostgreSQL database - fallback for data consistency
 */
@RestController
@RequestMapping("/api/historical")
@Tag(name = "F1 History", description = "Historical F1 seasons, races, and champions (1950-2026)")
public class HistoricalController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HistoricalController.class);

    @Autowired
    private ErgastService ergastService;

    @Autowired
    private HistoricalSeasonRepository seasonRepository;

    @Autowired
    private HistoricalRaceRepository raceRepository;

    @Autowired
    private HistoricalDriverRepository driverRepository;

    @Autowired
    private HistoricalConstructorRepository constructorRepository;

    @Autowired
    private HistoricalResultRepository resultRepository;

    /**
     * GET /api/historical/seasons
     * Returns all F1 seasons from Ergast API (cached)
     * Falls back to database if API unavailable
     */
    @GetMapping("/seasons")
    public ResponseEntity<?> getAllSeasons() {
        try {
            log.info("📡 [HistoricalController] GET /api/historical/seasons requested");
            
            // ✅ Try Ergast API first (with caching)
            List<Map<String, Object>> seasons = ergastService.getSeasons();
            log.info("✅ [HistoricalController] ErgastService returned {} seasons", seasons.size());
            
            if (!seasons.isEmpty()) {
                log.info("✅ [HistoricalController] Returning {} seasons from Ergast API", seasons.size());
                return ResponseEntity.ok(seasons);
            }
            
            // ✅ Fallback to database if Ergast is empty/down
            log.warn("⚠️ [HistoricalController] Ergast API returned empty, falling back to database");
            List<HistoricalSeason> dbSeasons = seasonRepository.findAll();
            dbSeasons.sort(Comparator.comparing(HistoricalSeason::getYear).reversed());
            
            // Convert database seasons to same format as Ergast
            List<Map<String, Object>> result = new ArrayList<>();
            for (HistoricalSeason season : dbSeasons) {
                Map<String, Object> seasonMap = new HashMap<>();
                seasonMap.put("year", season.getYear());
                seasonMap.put("id", season.getYear());
                result.add(seasonMap);
            }
            
            log.info("✅ [HistoricalController] Returned {} seasons from database", result.size());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("❌ [HistoricalController] Error fetching seasons: {} | {}", e.getClass().getSimpleName(), e.getMessage(), e);
            log.info("⚠️ [HistoricalController] Returning empty list as fallback");
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * GET /api/historical/season/{year}
     * Returns all races for a specific season from Ergast API (cached)
     * Falls back to database if API unavailable
     */
    @GetMapping("/season/{year}")
    public ResponseEntity<?> getSeasonDetail(@PathVariable Integer year) {
        try {
            log.info("📡 [HistoricalController] GET /api/historical/season/{} requested", year);
            
            // ✅ Try Ergast API first (with caching)
            List<Map<String, Object>> ergastRaces = ergastService.getRacesByYear(year);
            log.info("✅ [HistoricalController] ErgastService returned {} races for year {}", ergastRaces.size(), year);
            
            if (!ergastRaces.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("season", Map.of("year", year, "totalRounds", ergastRaces.size()));
                response.put("races", ergastRaces);
                response.put("raceCount", ergastRaces.size());
                log.info("✅ [HistoricalController] Returning {} races for season {} from Ergast API", ergastRaces.size(), year);
                return ResponseEntity.ok(response);
            }
            
            // ✅ Fallback to database if Ergast is empty/down
            log.warn("⚠️ [HistoricalController] Ergast API returned empty for year {}, falling back to database", year);
            Optional<HistoricalSeason> season = seasonRepository.findByYear(year);
            List<HistoricalRace> dbRaces = raceRepository.findBySeasonYearOrderByRound(year);

            // Convert database races to same format as Ergast
            List<Map<String, Object>> convertedRaces = new ArrayList<>();
            for (HistoricalRace race : dbRaces) {
                Map<String, Object> raceMap = new HashMap<>();
                raceMap.put("round", race.getRound());
                raceMap.put("id", race.getRound());
                raceMap.put("raceName", race.getRaceName());
                raceMap.put("date", race.getRaceDate());
                raceMap.put("circuitName", race.getCircuitName());
                raceMap.put("location", race.getCircuitCountry());
                raceMap.put("results", new ArrayList<>());
                raceMap.put("hasResults", false);
                convertedRaces.add(raceMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("season", season.isPresent() ? Map.of("year", season.get().getYear(), "totalRounds", convertedRaces.size()) : null);
            response.put("races", convertedRaces);
            response.put("raceCount", convertedRaces.size());

            log.info("✅ [HistoricalController] Returned {} races for season {} from database", convertedRaces.size(), year);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ [HistoricalController] Error fetching season {}: {} | {}", year, e.getClass().getSimpleName(), e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("season", null);
            response.put("races", new ArrayList<>());
            response.put("raceCount", 0);
            log.info("⚠️ [HistoricalController] Returning empty response as fallback");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * GET /api/historical/driver/{driverCode}/career
     * Returns driver career stats and race-by-race history
     */
    @GetMapping("/driver/{driverCode}/career")
    public ResponseEntity<?> getDriverCareer(@PathVariable String driverCode) {
        try {
            Optional<HistoricalDriver> driverOpt = driverRepository.findByCode(driverCode);
            if (driverOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            HistoricalDriver driver = driverOpt.get();
            List<HistoricalResult> results = resultRepository.findByDriverId(driver.getId());

            // Calculate career stats
            int wins = 0;
            int podiums = 0;
            int poles = 0;
            double avgFinish = 0;

            if (!results.isEmpty()) {
                wins = (int) results.stream()
                        .filter(r -> r.getFinishPosition() != null && r.getFinishPosition() == 1)
                        .count();
                podiums = (int) results.stream()
                        .filter(r -> r.getFinishPosition() != null && r.getFinishPosition() <= 3)
                        .count();
                poles = (int) results.stream()
                        .filter(r -> r.getGridPosition() != null && r.getGridPosition() == 1)
                        .count();

                avgFinish = results.stream()
                        .filter(r -> r.getFinishPosition() != null)
                        .mapToInt(HistoricalResult::getFinishPosition)
                        .average()
                        .orElse(0);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("driver", driver);
            response.put("careerStats", Map.of(
                    "races", results.size(),
                    "wins", wins,
                    "podiums", podiums,
                    "poles", poles,
                    "avgFinish", String.format("%.2f", avgFinish),
                    "championships", driver.getTotalChampionships()
            ));
            response.put("results", results);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.warn("Database query failed, returning empty response for driver {}: {}", driverCode, e.getMessage());
            // Return empty career stats if tables don't exist yet
            Map<String, Object> response = new HashMap<>();
            response.put("driver", null);
            response.put("careerStats", Map.of(
                    "races", 0,
                    "wins", 0,
                    "podiums", 0,
                    "poles", 0,
                    "avgFinish", "0.00",
                    "championships", 0
            ));
            response.put("results", new ArrayList<>());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * GET /api/historical/circuit/{circuitName}/history
     * Returns all races ever held at a specific circuit
     */

    @GetMapping("/circuit/{circuitName}/history")
    public ResponseEntity<?> getCircuitHistory(@PathVariable String circuitName) {
        try {
            List<HistoricalRace> races = raceRepository.findByCircuitName(circuitName);
            races.sort(Comparator.comparing(HistoricalRace::getSeasonYear).reversed());

            Map<String, Object> response = new HashMap<>();
            response.put("circuitName", circuitName);
            response.put("races", races);
            response.put("raceCount", races.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.warn("Database query failed, returning empty response for circuit {}: {}", circuitName, e.getMessage());
            // Return empty races list if tables don't exist yet
            Map<String, Object> response = new HashMap<>();
            response.put("circuitName", circuitName);
            response.put("races", new ArrayList<>());
            response.put("raceCount", 0);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * GET /api/historical/champions
     * Returns all F1 world champions from Ergast API (cached)
     * Falls back to database if API unavailable
     */
    @GetMapping("/champions")
    public ResponseEntity<?> getChampions() {
        try {
            // ✅ Try Ergast API first (with caching)
            List<Map<String, Object>> champions = ergastService.getChampions();
            
            if (!champions.isEmpty()) {
                log.info("✅ Returned {} champions from Ergast API", champions.size());
                return ResponseEntity.ok(champions);
            }
            
            // ✅ Fallback to database
            log.info("⚠️ Ergast API empty, falling back to database for champions");
            List<HistoricalSeason> seasons = seasonRepository.findAll();
            seasons.sort(Comparator.comparing(HistoricalSeason::getYear).reversed());

            List<Map<String, Object>> dbChampions = new ArrayList<>();
            for (HistoricalSeason season : seasons) {
                if (season.getChampionDriverId() != null) {
                    Map<String, Object> champion = new HashMap<>();
                    champion.put("year", season.getYear());
                    champion.put("driverId", season.getChampionDriverId());
                    champion.put("constructorId", season.getChampionConstructorId());

                    // Fetch driver info
                    driverRepository.findById(season.getChampionDriverId())
                            .ifPresent(driver -> {
                                champion.put("driverName", driver.getFullName());
                                champion.put("driverCode", driver.getCode());
                            });

                    // Fetch constructor info
                    if (season.getChampionConstructorId() != null) {
                        constructorRepository.findById(season.getChampionConstructorId())
                                .ifPresent(constructor -> {
                                    champion.put("constructorName", constructor.getName());
                                });
                    }

                    dbChampions.add(champion);
                }
            }

            return ResponseEntity.ok(dbChampions);

        } catch (Exception e) {
            log.warn("Error fetching champions: {}", e.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * GET /api/historical/records
     * Returns F1 all-time records
     */

    @GetMapping("/records")
    public ResponseEntity<?> getRecords() {
        try {
            Map<String, Object> records = new HashMap<>();

            // Most wins
            List<HistoricalDriver> allDrivers = driverRepository.findAll();
            Optional<HistoricalDriver> mostWinsDriver = allDrivers.stream()
                    .max(Comparator.comparing(HistoricalDriver::getTotalWins));

            // Most poles
            Optional<HistoricalDriver> mostPolesDriver = allDrivers.stream()
                    .max(Comparator.comparing(HistoricalDriver::getTotalPoles));

            // Most championships
            Optional<HistoricalDriver> mostChampDriver = allDrivers.stream()
                    .max(Comparator.comparing(HistoricalDriver::getTotalChampionships));

            // Constructor records
            List<HistoricalConstructor> allConstructors = constructorRepository.findAll();
            Optional<HistoricalConstructor> mostWinsConstructor = allConstructors.stream()
                    .max(Comparator.comparing(HistoricalConstructor::getTotalWins));

            // Build response
            if (mostWinsDriver.isPresent()) {
                HistoricalDriver driver = mostWinsDriver.get();
                records.put("mostWins", Map.of(
                        "driver", driver.getFullName(),
                        "wins", driver.getTotalWins()
                ));
            }

            if (mostPolesDriver.isPresent()) {
                HistoricalDriver driver = mostPolesDriver.get();
                records.put("mostPoles", Map.of(
                        "driver", driver.getFullName(),
                        "poles", driver.getTotalPoles()
                ));
            }

            if (mostChampDriver.isPresent()) {
                HistoricalDriver driver = mostChampDriver.get();
                records.put("mostChampionships", Map.of(
                        "driver", driver.getFullName(),
                        "championships", driver.getTotalChampionships()
                ));
            }

            if (mostWinsConstructor.isPresent()) {
                HistoricalConstructor constructor = mostWinsConstructor.get();
                records.put("constructorMostWins", Map.of(
                        "constructor", constructor.getName(),
                        "wins", constructor.getTotalWins()
                ));
            }

            return ResponseEntity.ok(records);

        } catch (Exception e) {
            log.warn("Database query failed, returning empty records: {}", e.getMessage());
            // Return empty records if tables don't exist yet
            return ResponseEntity.ok(new HashMap<>());
        }
    }

    /**
     * GET /api/historical/driver/{year}/{driverCode}/season
     * Returns driver performance for a specific season
     */

    @GetMapping("/driver/{year}/{driverCode}/season")
    public ResponseEntity<?> getDriverSeasonStats(@PathVariable Integer year, 
                                                   @PathVariable String driverCode) {
        try {
            Optional<HistoricalDriver> driverOpt = driverRepository.findByCode(driverCode);
            if (driverOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            HistoricalDriver driver = driverOpt.get();
            List<HistoricalResult> results = resultRepository.findByDriverId(driver.getId());

            // Filter results for this season
            List<HistoricalResult> seasonResults = results.stream()
                    .filter(r -> {
                        HistoricalRace race = raceRepository.findById(r.getRaceId()).orElse(null);
                        return race != null && race.getSeasonYear().equals(year);
                    })
                    .collect(Collectors.toList());

            // Calculate stats
            int wins = (int) seasonResults.stream()
                    .filter(r -> r.getFinishPosition() != null && r.getFinishPosition() == 1)
                    .count();
            int podiums = (int) seasonResults.stream()
                    .filter(r -> r.getFinishPosition() != null && r.getFinishPosition() <= 3)
                    .count();

            Map<String, Object> response = new HashMap<>();
            response.put("year", year);
            response.put("driver", driver);
            response.put("raceCount", seasonResults.size());
            response.put("wins", wins);
            response.put("podiums", podiums);
            response.put("results", seasonResults);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.warn("Database query failed, returning empty season stats for {} {}: {}", year, driverCode, e.getMessage());
            // Return empty season stats if tables don't exist yet
            Map<String, Object> response = new HashMap<>();
            response.put("year", year);
            response.put("driver", null);
            response.put("raceCount", 0);
            response.put("wins", 0);
            response.put("podiums", 0);
            response.put("results", new ArrayList<>());
            return ResponseEntity.ok(response);
        }
    }
}

