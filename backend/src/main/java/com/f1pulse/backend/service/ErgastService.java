package com.f1pulse.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ✅ ErgastService — Fetches F1 data from free Ergast API
 * 
 * Features:
 * 1. Fetches seasons, races, results from https://api.jolpi.ca/ergast/
 * 2. Built-in memory cache to avoid repeated API calls
 * 3. Handles errors gracefully with fallback to empty responses
 * 4. Supports seasons 1950-2026
 */
@Service
public class ErgastService {

    private static final Logger logger = LoggerFactory.getLogger(ErgastService.class);
    private static final String ERGAST_API_BASE = "https://api.jolpi.ca/ergast/f1";
    
    // ✅ In-memory cache: key = "season:year" or "races:year"
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public ErgastService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Get all seasons with limited details
     * Returns list of seasons from 1950 to current
     */
    public List<Map<String, Object>> getSeasons() {
        String cacheKey = "seasons:all";
        if (cache.containsKey(cacheKey)) {
            return (List<Map<String, Object>>) cache.get(cacheKey);
        }

        List<Map<String, Object>> seasons = new ArrayList<>();

        try {
            // Fetch available seasons from Ergast
            String url = ERGAST_API_BASE + ".json?limit=100";
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response != null && response.has("MRData")) {
                JsonNode mrData = response.get("MRData");
                if (mrData.has("SeasonTable") && mrData.get("SeasonTable").has("Seasons")) {
                    JsonNode seasonNodes = mrData.get("SeasonTable").get("Seasons");

                    for (JsonNode node : seasonNodes) {
                        Map<String, Object> season = new HashMap<>();
                        season.put("year", node.get("year").asInt());
                        season.put("rounds", node.get("url").asText()); // Store API URL
                        seasons.add(season);
                    }
                }
            }

            // Cache the result
            cache.put(cacheKey, seasons);
            logger.info("✅ Loaded {} seasons from Ergast API", seasons.size());

        } catch (Exception e) {
            logger.warn("⚠️ Failed to fetch seasons from Ergast: {}", e.getMessage());
            // Return empty list on failure — frontend will show appropriate message
        }

        return seasons;
    }

    /**
     * Get all races for a specific season
     * Returns list of races with circuit, date, winner info
     */
    public List<Map<String, Object>> getRacesByYear(Integer year) {
        String cacheKey = "races:" + year;
        if (cache.containsKey(cacheKey)) {
            return (List<Map<String, Object>>) cache.get(cacheKey);
        }

        List<Map<String, Object>> races = new ArrayList<>();

        try {
            // Fetch races for season
            String url = String.format("%s/%d/races.json", ERGAST_API_BASE, year);
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response != null && response.has("MRData")) {
                JsonNode mrData = response.get("MRData");
                if (mrData.has("RaceTable") && mrData.get("RaceTable").has("Races")) {
                    JsonNode raceNodes = mrData.get("RaceTable").get("Races");

                    for (JsonNode raceNode : raceNodes) {
                        Map<String, Object> race = new HashMap<>();
                        race.put("round", raceNode.get("round").asInt());
                        race.put("raceName", raceNode.get("name").asText());
                        race.put("date", raceNode.get("date").asText());

                        // Circuit info
                        JsonNode circuit = raceNode.get("Circuit");
                        if (circuit != null) {
                            race.put("circuitName", circuit.get("circuitName").asText());
                            race.put("location", circuit.get("Location").get("country").asText());
                        }

                        // Race results (if race has finished)
                        List<Map<String, Object>> results = getRaceResults(year, raceNode.get("round").asInt());
                        race.put("results", results);
                        race.put("hasResults", !results.isEmpty());

                        races.add(race);
                    }
                }
            }

            // Cache the result
            cache.put(cacheKey, races);
            logger.info("✅ Loaded {} races for season {} from Ergast API", races.size(), year);

        } catch (Exception e) {
            logger.warn("⚠️ Failed to fetch races for year {} from Ergast: {}", year, e.getMessage());
        }

        return races;
    }

    /**
     * Get results for a specific race (round in a season)
     */
    private List<Map<String, Object>> getRaceResults(Integer year, Integer round) {
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            // Fetch results for specific race
            String url = String.format("%s/%d/%d/results.json", ERGAST_API_BASE, year, round);
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response != null && response.has("MRData")) {
                JsonNode mrData = response.get("MRData");
                if (mrData.has("RaceTable") && mrData.get("RaceTable").has("Races")) {
                    JsonNode races = mrData.get("RaceTable").get("Races");
                    if (races.isArray() && races.size() > 0) {
                        JsonNode race = races.get(0);
                        
                        // Get race winner
                        if (race.has("Results")) {
                            for (JsonNode result : race.get("Results")) {
                                Map<String, Object> finisher = new HashMap<>();
                                finisher.put("position", result.get("position").asInt());

                                // Driver info
                                JsonNode driver = result.get("Driver");
                                if (driver != null) {
                                    finisher.put("driverName", driver.get("familyName").asText());
                                    finisher.put("driverCode", driver.get("code").asText());
                                }

                                // Constructor info
                                JsonNode constructor = result.get("Constructor");
                                if (constructor != null) {
                                    finisher.put("constructor", constructor.get("name").asText());
                                }

                                finisher.put("gridPosition", result.get("grid").asInt());
                                finisher.put("points", result.get("points").asInt());

                                results.add(finisher);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.debug("Could not fetch detailed results for {}/{}: {}", year, round, e.getMessage());
        }

        return results;
    }

    /**
     * Get all champions (world champions by year)
     */
    public List<Map<String, Object>> getChampions() {
        String cacheKey = "champions:all";
        if (cache.containsKey(cacheKey)) {
            return (List<Map<String, Object>>) cache.get(cacheKey);
        }

        List<Map<String, Object>> champions = new ArrayList<>();

        try {
            // Fetch driver standings for key years to extract champions
            for (int year = 1950; year <= 2026; year++) {
                try {
                    String url = String.format("%s/%d/driverStandings.json", ERGAST_API_BASE, year);
                    JsonNode response = restTemplate.getForObject(url, JsonNode.class);

                    if (response != null && response.has("MRData")) {
                        JsonNode mrData = response.get("MRData");
                        if (mrData.has("StandingsTable") && mrData.get("StandingsTable").has("StandingsLists")) {
                            JsonNode standingsLists = mrData.get("StandingsTable").get("StandingsLists");
                            if (standingsLists.isArray() && standingsLists.size() > 0) {
                                JsonNode standings = standingsLists.get(0);
                                
                                // First driver in final standings is champion
                                if (standings.has("DriverStandings")) {
                                    JsonNode driverStandings = standings.get("DriverStandings");
                                    if (driverStandings.isArray() && driverStandings.size() > 0) {
                                        JsonNode firstPlace = driverStandings.get(0);
                                        JsonNode driver = firstPlace.get("Driver");

                                        Map<String, Object> champion = new HashMap<>();
                                        champion.put("year", year);
                                        champion.put("driverName", driver.get("familyName").asText());
                                        champion.put("firstName", driver.get("givenName").asText());
                                        champion.put("nationality", driver.get("nationality").asText());
                                        champion.put("points", firstPlace.get("points").asInt());

                                        champions.add(champion);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Skip years with no data
                    logger.debug("No data for year {}", year);
                }
            }

            // Cache the result
            cache.put(cacheKey, champions);
            logger.info("✅ Loaded {} champions from Ergast API", champions.size());

        } catch (Exception e) {
            logger.warn("⚠️ Failed to fetch champions from Ergast: {}", e.getMessage());
        }

        return champions;
    }

    /**
     * Clear cache (useful for testing or manual refresh)
     */
    public void clearCache() {
        cache.clear();
        logger.info("✅ Cache cleared");
    }
}
