package com.f1pulse.backend.service;

import com.f1pulse.backend.model.*;
import com.f1pulse.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service to ingest historical F1 data from Ergast API
 * Fetches data for seasons 1950-2026 and stores in database
 */
@Service
public class HistoricalDataIngestionService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HistoricalDataIngestionService.class);

    private static final String ERGAST_BASE_URL = "https://ergast.com/api/f1";
    private static final int RATE_LIMIT_DELAY_MS = 500;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Progress tracking for async operations
    private static final Map<String, IngestionProgress> progressMap = new ConcurrentHashMap<>();

    @Autowired
    private RestTemplate restTemplate;

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

    // Additional repositories for qualifying and standings
    @Autowired(required = false)
    private com.f1pulse.backend.repository.HistoricalQualifyingRepository qualifyingRepository;

    @Autowired(required = false)
    private com.f1pulse.backend.repository.HistoricalDriverStandingsRepository driverStandingsRepository;

    @Autowired(required = false)
    private com.f1pulse.backend.repository.HistoricalConstructorStandingsRepository constructorStandingsRepository;

    /**
     * Ingests all F1 historical data from 1950 to current year (async)
     */
    @Async
    @Transactional
    public void ingestAllHistoricalData(String jobId, Integer fromYear, Integer toYear) {
        log.info("🚀 Starting full F1 historical data ingestion ({}-{})", fromYear, toYear);
        
        IngestionProgress progress = new IngestionProgress();
        progress.setJobId(jobId);
        progress.setFromYear(fromYear);
        progress.setToYear(toYear);
        progress.setCurrentYear(fromYear);
        progress.setTotalYears(toYear - fromYear + 1);
        progress.setStatus("IN_PROGRESS");
        progress.setStartTime(new Date());
        progressMap.put(jobId, progress);
        
        long startTime = System.currentTimeMillis();
        List<String> errors = new ArrayList<>();

        try {
            for (int year = fromYear; year <= toYear; year++) {
                try {
                    progress.setCurrentYear(year);
                    progressMap.put(jobId, progress);
                    
                    // Check if season already exists (sync strategy)
                    if (seasonRepository.findByYear(year).isPresent()) {
                        log.info("⏭️  Season {} already exists, skipping", year);
                        continue;
                    }
                    
                    ingestSeason(year);
                    Thread.sleep(RATE_LIMIT_DELAY_MS); // Rate limiting
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Ingestion interrupted", e);
                } catch (Exception e) {
                    log.error("❌ Error ingesting season {}: {}", year, e.getMessage());
                    errors.add("Year " + year + ": " + e.getMessage());
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            progress.setStatus("COMPLETED");
            progress.setEndTime(new Date());
            progress.setDurationMs(duration);
            progress.setErrors(errors);
            progressMap.put(jobId, progress);
            log.info("✅ Full historical data ingestion completed in {} ms", duration);

        } catch (Exception e) {
            log.error("❌ Error during full historical ingestion", e);
            progress.setStatus("FAILED");
            progress.setEndTime(new Date());
            progress.setErrors(errors);
            progressMap.put(jobId, progress);
            throw new RuntimeException(e);
        }
    }

    /**
     * Ingests data for a single season
     */
    @Transactional
    public void ingestSeason(Integer year) {
        log.info("📅 Ingesting season: {}", year);

        try {
            // Fetch and store season info
            Map<String, Object> seasonData = fetchSeasonData(year);
            if (seasonData == null) {
                log.warn("⚠️  No data found for season {}", year);
                return;
            }

            // Create or update season (upsert strategy)
            HistoricalSeason season = seasonRepository.findByYear(year)
                    .orElse(new HistoricalSeason(year, 0));
            season.setYear(year);
            if (seasonData.containsKey("totalRounds")) {
                season.setTotalRounds((Integer) seasonData.get("totalRounds"));
            }
            seasonRepository.save(season);

            Thread.sleep(RATE_LIMIT_DELAY_MS);

            // Fetch and store races
            List<Map<String, Object>> races = fetchRaces(year);
            for (Map<String, Object> raceData : races) {
                storeRace(year, raceData);
                Thread.sleep(RATE_LIMIT_DELAY_MS);
            }

            // Fetch and store results
            List<Map<String, Object>> results = fetchResults(year);
            int resultCount = 0;
            for (Map<String, Object> resultData : results) {
                storeResult(resultData);
                resultCount++;
            }
            
            // Fetch and store qualifying results
            List<Map<String, Object>> qualifying = fetchQualifying(year);
            int qualifyingCount = 0;
            for (Map<String, Object> qualData : qualifying) {
                storeQualifying(qualData);
                qualifyingCount++;
            }
            
            // Fetch and store driver standings
            List<Map<String, Object>> driverStandings = fetchDriverStandings(year);
            for (Map<String, Object> standingData : driverStandings) {
                storeDriverStanding(year, standingData);
            }
            
            // Fetch and store constructor standings
            List<Map<String, Object>> constructorStandings = fetchConstructorStandings(year);
            for (Map<String, Object> standingData : constructorStandings) {
                storeConstructorStanding(year, standingData);
            }
            
            log.info("✅ Season {} completed: {} races, {} results, {} qualifying, {} driver standings, {} constructor standings ingested", 
                    year, races.size(), resultCount, qualifyingCount, driverStandings.size(), constructorStandings.size());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ingestion interrupted", e);
        } catch (Exception e) {
            log.error("❌ Error ingesting season {}: {}", year, e.getMessage(), e);
        }
    }

    // ============= ERGAST API CALLS =============

    /**
     * Fetches season data from Ergast API
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchSeasonData(Integer year) {
        try {
            String url = String.format("%s/%d.json", ERGAST_BASE_URL, year);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("MRData")) {
                return null;
            }

            Map<String, Object> mrData = (Map<String, Object>) response.get("MRData");
            List<Map<String, Object>> seasons = (List<Map<String, Object>>) mrData.get("SeasonTable");

            if (seasons == null || seasons.isEmpty()) {
                return null;
            }

            Map<String, Object> season = seasons.get(0);
            Map<String, Object> result = new HashMap<>();
            result.put("year", year);
            result.put("totalRounds", Integer.parseInt((String) season.getOrDefault("round", "0")));

            return result;

        } catch (Exception e) {
            log.warn("⚠️  Could not fetch season data for {}: {}", year, e.getMessage());
            return null;
        }
    }

    /**
     * Fetches all races for a given season
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchRaces(Integer year) {
        List<Map<String, Object>> races = new ArrayList<>();
        int offset = 0;
        int limit = 1000;
        boolean hasMore = true;

        while (hasMore) {
            try {
                String url = String.format("%s/%d/races.json?offset=%d&limit=%d", 
                        ERGAST_BASE_URL, year, offset, limit);
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);

                if (response == null || !response.containsKey("MRData")) {
                    break;
                }

                Map<String, Object> mrData = (Map<String, Object>) response.get("MRData");
                List<Map<String, Object>> raceTable = (List<Map<String, Object>>) 
                        mrData.getOrDefault("RaceTable", new ArrayList<>());

                if (raceTable.isEmpty()) {
                    hasMore = false;
                } else {
                    races.addAll(raceTable);
                    offset += limit;
                    // Check if we got less than limit (indicating end of data)
                    if (raceTable.size() < limit) {
                        hasMore = false;
                    }
                }

                Thread.sleep(RATE_LIMIT_DELAY_MS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted during race fetch", e);
            } catch (Exception e) {
                log.warn("⚠️  Error fetching races for {}: {}", year, e.getMessage());
                hasMore = false;
            }
        }

        return races;
    }

    /**
     * Fetches all results for a given season
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchResults(Integer year) {
        List<Map<String, Object>> results = new ArrayList<>();
        int offset = 0;
        int limit = 1000;
        boolean hasMore = true;

        while (hasMore) {
            try {
                String url = String.format("%s/%d/results.json?offset=%d&limit=%d", 
                        ERGAST_BASE_URL, year, offset, limit);
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);

                if (response == null || !response.containsKey("MRData")) {
                    break;
                }

                Map<String, Object> mrData = (Map<String, Object>) response.get("MRData");
                List<Map<String, Object>> resultTable = (List<Map<String, Object>>) 
                        mrData.getOrDefault("RaceTable", new ArrayList<>());

                if (resultTable.isEmpty()) {
                    hasMore = false;
                } else {
                    // Extract results from races
                    for (Map<String, Object> race : resultTable) {
                        List<Map<String, Object>> raceResults = (List<Map<String, Object>>) 
                                race.getOrDefault("Results", new ArrayList<>());
                        raceResults.forEach(r -> {
                            r.put("raceData", race); // Attach race info to each result
                            results.add(r);
                        });
                    }

                    offset += limit;
                    if (resultTable.size() < limit) {
                        hasMore = false;
                    }
                }

                Thread.sleep(RATE_LIMIT_DELAY_MS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted during results fetch", e);
            } catch (Exception e) {
                log.warn("⚠️  Error fetching results for {}: {}", year, e.getMessage());
                hasMore = false;
            }
        }

        return results;
    }

    /**
     * Fetches all qualifying results for a given season
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchQualifying(Integer year) {
        List<Map<String, Object>> qualifying = new ArrayList<>();
        int offset = 0;
        int limit = 1000;
        boolean hasMore = true;

        while (hasMore) {
            try {
                String url = String.format("%s/%d/qualifying.json?offset=%d&limit=%d", 
                        ERGAST_BASE_URL, year, offset, limit);
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);

                if (response == null || !response.containsKey("MRData")) {
                    break;
                }

                Map<String, Object> mrData = (Map<String, Object>) response.get("MRData");
                List<Map<String, Object>> raceTable = (List<Map<String, Object>>) 
                        mrData.getOrDefault("RaceTable", new ArrayList<>());

                if (raceTable.isEmpty()) {
                    hasMore = false;
                } else {
                    // Extract qualifying from races
                    for (Map<String, Object> race : raceTable) {
                        List<Map<String, Object>> raceQualifying = (List<Map<String, Object>>) 
                                race.getOrDefault("QualifyingResults", new ArrayList<>());
                        raceQualifying.forEach(q -> {
                            q.put("raceData", race); // Attach race info to each qualifying result
                            qualifying.add(q);
                        });
                    }

                    offset += limit;
                    if (raceTable.size() < limit) {
                        hasMore = false;
                    }
                }

                Thread.sleep(RATE_LIMIT_DELAY_MS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted during qualifying fetch", e);
            } catch (Exception e) {
                log.warn("⚠️  Error fetching qualifying for {}: {}", year, e.getMessage());
                hasMore = false;
            }
        }

        return qualifying;
    }

    /**
     * Fetches driver standings for a given season
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchDriverStandings(Integer year) {
        List<Map<String, Object>> standings = new ArrayList<>();

        try {
            String url = String.format("%s/%d/driverStandings.json", ERGAST_BASE_URL, year);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("MRData")) {
                Map<String, Object> mrData = (Map<String, Object>) response.get("MRData");
                List<Map<String, Object>> standingsTable = (List<Map<String, Object>>) 
                        mrData.getOrDefault("StandingsTable", new ArrayList<>());

                if (!standingsTable.isEmpty()) {
                    Map<String, Object> standingsList = standingsTable.get(0);
                    List<Map<String, Object>> driverStandings = (List<Map<String, Object>>) 
                            standingsList.getOrDefault("DriverStandings", new ArrayList<>());
                    standings.addAll(driverStandings);
                }
            }

            Thread.sleep(RATE_LIMIT_DELAY_MS);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during driver standings fetch", e);
        } catch (Exception e) {
            log.warn("⚠️  Error fetching driver standings for {}: {}", year, e.getMessage());
        }

        return standings;
    }

    /**
     * Fetches constructor standings for a given season
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchConstructorStandings(Integer year) {
        List<Map<String, Object>> standings = new ArrayList<>();

        try {
            String url = String.format("%s/%d/constructorStandings.json", ERGAST_BASE_URL, year);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("MRData")) {
                Map<String, Object> mrData = (Map<String, Object>) response.get("MRData");
                List<Map<String, Object>> standingsTable = (List<Map<String, Object>>) 
                        mrData.getOrDefault("StandingsTable", new ArrayList<>());

                if (!standingsTable.isEmpty()) {
                    Map<String, Object> standingsList = standingsTable.get(0);
                    List<Map<String, Object>> constructorStandings = (List<Map<String, Object>>) 
                            standingsList.getOrDefault("ConstructorStandings", new ArrayList<>());
                    standings.addAll(constructorStandings);
                }
            }

            Thread.sleep(RATE_LIMIT_DELAY_MS);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during constructor standings fetch", e);
        } catch (Exception e) {
            log.warn("⚠️  Error fetching constructor standings for {}: {}", year, e.getMessage());
        }

        return standings;
    }

    // ============= DATA STORAGE =============

    /**
     * Stores a race in the database
     */
    @SuppressWarnings("unchecked")
    private void storeRace(Integer year, Map<String, Object> raceData) {
        try {
            Integer round = Integer.parseInt((String) raceData.getOrDefault("round", "0"));
            String raceName = (String) raceData.get("name");

            // Find or create race
            HistoricalRace race = raceRepository.findBySeasonYearAndRound(year, round)
                    .orElse(new HistoricalRace());

            race.setSeasonYear(year);
            race.setRound(round);
            race.setRaceName(raceName);
            race.setStatus("COMPLETED");

            // Extract circuit info
            Map<String, Object> circuit = (Map<String, Object>) raceData.get("Circuit");
            if (circuit != null) {
                race.setCircuitName((String) circuit.get("circuitName"));
                Map<String, Object> location = (Map<String, Object>) circuit.get("Location");
                if (location != null) {
                    race.setCircuitCountry((String) location.get("country"));
                }
            }

            // Parse race date
            String dateStr = (String) raceData.get("date");
            if (dateStr != null && !dateStr.isEmpty()) {
                try {
                    race.setRaceDate(LocalDate.parse(dateStr, DATE_FORMATTER));
                } catch (Exception e) {
                    log.warn("Could not parse race date: {}", dateStr);
                }
            }

            raceRepository.save(race);

        } catch (Exception e) {
            log.warn("⚠️  Error storing race: {}", e.getMessage());
        }
    }

    /**
     * Stores a race result in the database
     */
    @SuppressWarnings("unchecked")
    private void storeResult(Map<String, Object> resultData) {
        try {
            // Get race info (attached by fetchResults)
            Map<String, Object> raceData = (Map<String, Object>) resultData.get("raceData");
            if (raceData == null) return;

            Integer year = Integer.parseInt((String) raceData.get("season"));
            Integer round = Integer.parseInt((String) raceData.get("round"));

            // Find race
            HistoricalRace race = raceRepository.findBySeasonYearAndRound(year, round)
                    .orElse(null);
            if (race == null) return;

            // Get driver info
            Map<String, Object> driver = (Map<String, Object>) resultData.get("Driver");
            if (driver == null) return;

            String driverRef = (String) driver.get("driverId");
            HistoricalDriver historicalDriver = driverRepository.findByDriverRef(driverRef)
                    .orElse(new HistoricalDriver());

            if (historicalDriver.getId() == null) {
                historicalDriver.setDriverRef(driverRef);
                historicalDriver.setCode((String) driver.getOrDefault("code", ""));
                historicalDriver.setFullName(driver.get("givenName") + " " + driver.get("familyName"));
                historicalDriver.setNationality((String) driver.get("nationality"));
                driverRepository.save(historicalDriver);
            }

            // Get constructor info
            Map<String, Object> constructor = (Map<String, Object>) resultData.get("Constructor");
            HistoricalConstructor historicalConstructor = null;
            if (constructor != null) {
                String constructorRef = (String) constructor.get("constructorId");
                historicalConstructor = constructorRepository.findByConstructorRef(constructorRef)
                        .orElse(new HistoricalConstructor());

                if (historicalConstructor.getId() == null) {
                    historicalConstructor.setConstructorRef(constructorRef);
                    historicalConstructor.setName((String) constructor.get("name"));
                    historicalConstructor.setNationality((String) constructor.get("nationality"));
                    constructorRepository.save(historicalConstructor);
                }
            }

            // Create or update result
            HistoricalResult result = resultRepository
                    .findByRaceIdAndDriverId(race.getId(), historicalDriver.getId())
                    .orElse(new HistoricalResult());

            result.setRaceId(race.getId());
            result.setDriverId(historicalDriver.getId());
            if (historicalConstructor != null) {
                result.setConstructorId(historicalConstructor.getId());
            }

            // Parse grid and finish position
            String gridStr = (String) resultData.get("grid");
            if (gridStr != null && !gridStr.isEmpty() && !"0".equals(gridStr)) {
                result.setGridPosition(Integer.parseInt(gridStr));
            }

            String positionStr = (String) resultData.get("position");
            if (positionStr != null && !positionStr.isEmpty() && !"R".equals(positionStr)) {
                try {
                    result.setFinishPosition(Integer.parseInt(positionStr));
                } catch (NumberFormatException e) {
                    // Position might be "R" or other non-numeric value
                }
            }

            // Parse points
            String pointsStr = (String) resultData.get("points");
            if (pointsStr != null && !pointsStr.isEmpty()) {
                try {
                    result.setPoints(new BigDecimal(pointsStr));
                } catch (NumberFormatException e) {
                    result.setPoints(BigDecimal.ZERO);
                }
            }

            result.setStatus((String) resultData.getOrDefault("status", "Finished"));

            resultRepository.save(result);

        } catch (Exception e) {
            log.warn("⚠️  Error storing result: {}", e.getMessage());
        }
    }

    /**
     * Stores a qualifying result in the database
     */
    @SuppressWarnings("unchecked")
    private void storeQualifying(Map<String, Object> qualData) {
        try {
            if (qualifyingRepository == null) {
                log.debug("Qualifying repository not available, skipping");
                return;
            }

            // Get race info
            Map<String, Object> raceData = (Map<String, Object>) qualData.get("raceData");
            if (raceData == null) return;

            Integer year = Integer.parseInt((String) raceData.get("season"));
            Integer round = Integer.parseInt((String) raceData.get("round"));

            // Find race
            HistoricalRace race = raceRepository.findBySeasonYearAndRound(year, round)
                    .orElse(null);
            if (race == null) return;

            // Get driver info
            Map<String, Object> driver = (Map<String, Object>) qualData.get("Driver");
            if (driver == null) return;

            String driverRef = (String) driver.get("driverId");
            HistoricalDriver historicalDriver = driverRepository.findByDriverRef(driverRef)
                    .orElse(null);
            if (historicalDriver == null) return;

            // Get constructor info
            Map<String, Object> constructor = (Map<String, Object>) qualData.get("Constructor");
            HistoricalConstructor historicalConstructor = null;
            if (constructor != null) {
                String constructorRef = (String) constructor.get("constructorId");
                historicalConstructor = constructorRepository.findByConstructorRef(constructorRef)
                        .orElse(null);
            }

            // Create or update qualifying result
            com.f1pulse.backend.model.HistoricalQualifying qualifying = 
                    qualifyingRepository.findByRaceIdAndDriverId(race.getId(), historicalDriver.getId())
                    .orElse(new com.f1pulse.backend.model.HistoricalQualifying());

            qualifying.setRaceId(race.getId());
            qualifying.setDriverId(historicalDriver.getId());
            if (historicalConstructor != null) {
                qualifying.setConstructorId(historicalConstructor.getId());
            }

            // Parse position
            String positionStr = (String) qualData.get("position");
            if (positionStr != null && !positionStr.isEmpty()) {
                try {
                    qualifying.setPosition(Integer.parseInt(positionStr));
                } catch (NumberFormatException e) {
                    // Position might be non-numeric
                }
            }

            // Parse Q1, Q2, Q3 times
            qualifying.setQ1((String) qualData.get("Q1"));
            qualifying.setQ2((String) qualData.get("Q2"));
            qualifying.setQ3((String) qualData.get("Q3"));

            qualifyingRepository.save(qualifying);

        } catch (Exception e) {
            log.warn("⚠️  Error storing qualifying: {}", e.getMessage());
        }
    }

    /**
     * Stores a driver standing in the database
     */
    @SuppressWarnings("unchecked")
    private void storeDriverStanding(Integer year, Map<String, Object> standingData) {
        try {
            if (driverStandingsRepository == null) {
                log.debug("Driver standings repository not available, skipping");
                return;
            }

            // Get driver info
            Map<String, Object> driver = (Map<String, Object>) standingData.get("Driver");
            if (driver == null) return;

            String driverRef = (String) driver.get("driverId");
            HistoricalDriver historicalDriver = driverRepository.findByDriverRef(driverRef)
                    .orElse(null);
            if (historicalDriver == null) return;

            // Create or update standing
            com.f1pulse.backend.model.HistoricalDriverStandings standing = 
                    driverStandingsRepository.findByYearAndDriverId(year, historicalDriver.getId())
                    .orElse(new com.f1pulse.backend.model.HistoricalDriverStandings());

            standing.setYear(year);
            standing.setDriverId(historicalDriver.getId());

            // Parse position
            String positionStr = (String) standingData.get("position");
            if (positionStr != null && !positionStr.isEmpty()) {
                try {
                    standing.setPosition(Integer.parseInt(positionStr));
                } catch (NumberFormatException e) {
                    // Position might be non-numeric
                }
            }

            // Parse points
            String pointsStr = (String) standingData.get("points");
            if (pointsStr != null && !pointsStr.isEmpty()) {
                try {
                    standing.setPoints(new BigDecimal(pointsStr));
                } catch (NumberFormatException e) {
                    standing.setPoints(BigDecimal.ZERO);
                }
            }

            // Parse wins
            String winsStr = (String) standingData.get("wins");
            if (winsStr != null && !winsStr.isEmpty()) {
                try {
                    standing.setWins(Integer.parseInt(winsStr));
                } catch (NumberFormatException e) {
                    standing.setWins(0);
                }
            }

            driverStandingsRepository.save(standing);

        } catch (Exception e) {
            log.warn("⚠️  Error storing driver standing: {}", e.getMessage());
        }
    }

    /**
     * Stores a constructor standing in the database
     */
    @SuppressWarnings("unchecked")
    private void storeConstructorStanding(Integer year, Map<String, Object> standingData) {
        try {
            if (constructorStandingsRepository == null) {
                log.debug("Constructor standings repository not available, skipping");
                return;
            }

            // Get constructor info
            Map<String, Object> constructor = (Map<String, Object>) standingData.get("Constructor");
            if (constructor == null) return;

            String constructorRef = (String) constructor.get("constructorId");
            HistoricalConstructor historicalConstructor = constructorRepository.findByConstructorRef(constructorRef)
                    .orElse(null);
            if (historicalConstructor == null) return;

            // Create or update standing
            com.f1pulse.backend.model.HistoricalConstructorStandings standing = 
                    constructorStandingsRepository.findByYearAndConstructorId(year, historicalConstructor.getId())
                    .orElse(new com.f1pulse.backend.model.HistoricalConstructorStandings());

            standing.setYear(year);
            standing.setConstructorId(historicalConstructor.getId());

            // Parse position
            String positionStr = (String) standingData.get("position");
            if (positionStr != null && !positionStr.isEmpty()) {
                try {
                    standing.setPosition(Integer.parseInt(positionStr));
                } catch (NumberFormatException e) {
                    // Position might be non-numeric
                }
            }

            // Parse points
            String pointsStr = (String) standingData.get("points");
            if (pointsStr != null && !pointsStr.isEmpty()) {
                try {
                    standing.setPoints(new BigDecimal(pointsStr));
                } catch (NumberFormatException e) {
                    standing.setPoints(BigDecimal.ZERO);
                }
            }

            // Parse wins
            String winsStr = (String) standingData.get("wins");
            if (winsStr != null && !winsStr.isEmpty()) {
                try {
                    standing.setWins(Integer.parseInt(winsStr));
                } catch (NumberFormatException e) {
                    standing.setWins(0);
                }
            }

            constructorStandingsRepository.save(standing);

        } catch (Exception e) {
            log.warn("⚠️  Error storing constructor standing: {}", e.getMessage());
        }
    }

    /**
     * Get current ingestion status
     */
    public Map<String, Object> getIngestionStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("totalRaces", raceRepository.count());
        status.put("totalResults", resultRepository.count());
        status.put("totalDrivers", driverRepository.count());
        status.put("totalConstructors", constructorRepository.count());
        status.put("yearsIngested", seasonRepository.count());
        status.put("lastUpdated", new Date());
        
        // Add qualifying and standings counts if repositories exist
        try {
            if (qualifyingRepository != null) {
                status.put("totalQualifying", qualifyingRepository.count());
            }
            if (driverStandingsRepository != null) {
                status.put("totalDriverStandings", driverStandingsRepository.count());
            }
            if (constructorStandingsRepository != null) {
                status.put("totalConstructorStandings", constructorStandingsRepository.count());
            }
        } catch (Exception e) {
            // Repositories might not exist yet
        }
        
        return status;
    }
    
    /**
     * Get progress for a specific async ingestion job
     */
    public Map<String, Object> getIngestionProgress(String jobId) {
        IngestionProgress progress = progressMap.get(jobId);
        if (progress == null) {
            return Map.of("error", "Job not found", "jobId", jobId);
        }
        return progress.toMap();
    }
    
    /**
     * Inner class for tracking ingestion progress
     */
    private static class IngestionProgress {
        private String jobId;
        private Integer fromYear;
        private Integer toYear;
        private Integer currentYear;
        private Integer totalYears;
        private String status;
        private Date startTime;
        private Date endTime;
        private Long durationMs;
        private List<String> errors = new ArrayList<>();
        
        // Getters and setters
        public String getJobId() { return jobId; }
        public void setJobId(String jobId) { this.jobId = jobId; }
        public Integer getFromYear() { return fromYear; }
        public void setFromYear(Integer fromYear) { this.fromYear = fromYear; }
        public Integer getToYear() { return toYear; }
        public void setToYear(Integer toYear) { this.toYear = toYear; }
        public Integer getCurrentYear() { return currentYear; }
        public void setCurrentYear(Integer currentYear) { this.currentYear = currentYear; }
        public Integer getTotalYears() { return totalYears; }
        public void setTotalYears(Integer totalYears) { this.totalYears = totalYears; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Date getStartTime() { return startTime; }
        public void setStartTime(Date startTime) { this.startTime = startTime; }
        public Date getEndTime() { return endTime; }
        public void setEndTime(Date endTime) { this.endTime = endTime; }
        public Long getDurationMs() { return durationMs; }
        public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("jobId", jobId);
            map.put("fromYear", fromYear);
            map.put("toYear", toYear);
            map.put("currentYear", currentYear);
            map.put("totalYears", totalYears);
            map.put("status", status);
            map.put("startTime", startTime);
            map.put("endTime", endTime);
            map.put("durationMs", durationMs);
            map.put("errors", errors);
            if (totalYears != null && currentYear != null) {
                double progress = ((double)(currentYear - fromYear) / totalYears) * 100;
                map.put("progressPercent", Math.round(progress));
            }
            return map;
        }
    }
}
