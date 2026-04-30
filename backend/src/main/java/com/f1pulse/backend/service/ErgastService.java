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
            logger.info("✅ [ErgastService] Found {} seasons in cache", ((List<?>) cache.get(cacheKey)).size());
            return (List<Map<String, Object>>) cache.get(cacheKey);
        }

        List<Map<String, Object>> seasons = new ArrayList<>();

        try {
            // Fetch available seasons from Ergast
            String url = ERGAST_API_BASE + "/seasons.json?limit=100";
            logger.info("📡 [ErgastService] Fetching seasons from: {}", url);
            
            JsonNode response = null;
            try {
                response = restTemplate.getForObject(url, JsonNode.class);
            } catch (Exception e) {
                logger.error("❌ [ErgastService] RestTemplate error fetching seasons: {} | {}", e.getClass().getSimpleName(), e.getMessage());
                throw e;
            }
            
            if (response == null) {
                logger.error("❌ [ErgastService] Null response from Ergast API for seasons");
                return seasons;
            }
            
            logger.info("✅ [ErgastService] Got response from Ergast API, response is null? {}", response == null);
            
            // 🔍 DEBUG: Print raw response
            String rawJson = objectMapper.writeValueAsString(response);
            logger.info("📋 [ErgastService] RAW API RESPONSE (first 500 chars): {}", rawJson.substring(0, Math.min(500, rawJson.length())));
            logger.info("📋 [ErgastService] RAW RESPONSE LENGTH: {} chars", rawJson.length());

            if (response.has("MRData")) {
                JsonNode mrData = response.get("MRData");
                logger.info("✅ [ErgastService] Found MRData object");
                
                if (mrData.has("SeasonTable")) {
                    JsonNode seasonTable = mrData.get("SeasonTable");
                    logger.info("✅ [ErgastService] Found SeasonTable object");
                    
                    if (seasonTable.has("Seasons")) {
                        JsonNode seasonNodes = seasonTable.get("Seasons");
                        logger.info("✅ [ErgastService] Found Seasons array with {} entries", seasonNodes.size());

                        for (JsonNode node : seasonNodes) {
                            try {
                                Map<String, Object> season = new HashMap<>();
                                season.put("year", node.get("year").asInt());
                                season.put("id", node.get("year").asInt());
                                seasons.add(season);
                            } catch (Exception e) {
                                logger.warn("⚠️ [ErgastService] Error parsing season node: {}", e.getMessage());
                            }
                        }
                        logger.info("✅ [ErgastService] Successfully parsed {} seasons", seasons.size());
                    } else {
                        logger.error("❌ [ErgastService] Seasons field not found in SeasonTable. Available fields: {}", seasonTable.fieldNames());
                    }
                } else {
                    logger.error("❌ [ErgastService] SeasonTable not found in MRData. Available fields: {}", mrData.fieldNames());
                }
            } else {
                logger.error("❌ [ErgastService] MRData not found in response. Available fields: {}", response.fieldNames());
            }

            // Cache the result only if we got data
            if (!seasons.isEmpty()) {
                cache.put(cacheKey, seasons);
                logger.info("✅ [ErgastService] Cached {} seasons from Ergast API", seasons.size());
            } else {
                logger.warn("⚠️ [ErgastService] No seasons parsed, NOT caching empty result");
            }

        } catch (Exception e) {
            logger.error("❌ [ErgastService] Exception while fetching seasons: {} | {}", e.getClass().getSimpleName(), e.getMessage(), e);
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
            logger.info("✅ [ErgastService] Found {} races for year {} in cache", ((List<?>) cache.get(cacheKey)).size(), year);
            return (List<Map<String, Object>>) cache.get(cacheKey);
        }

        List<Map<String, Object>> races = new ArrayList<>();

        try {
            // Fetch races for season
            String url = String.format("%s/%d/races.json", ERGAST_API_BASE, year);
            logger.info("📡 [ErgastService] Fetching races from: {}", url);
            
            JsonNode response = null;
            try {
                response = restTemplate.getForObject(url, JsonNode.class);
            } catch (Exception e) {
                logger.error("❌ [ErgastService] RestTemplate error fetching races for year {}: {} | {}", year, e.getClass().getSimpleName(), e.getMessage());
                throw e;
            }
            
            if (response == null) {
                logger.error("❌ [ErgastService] Null response from Ergast API for year {}", year);
                return races;
            }
            
            logger.info("✅ [ErgastService] Got response from Ergast API for year {}", year);
            
            // 🔍 DEBUG: Print raw response
            String rawJson = objectMapper.writeValueAsString(response);
            logger.info("📋 [ErgastService] RAW RACES RESPONSE for {} (first 500 chars): {}", year, rawJson.substring(0, Math.min(500, rawJson.length())));

            if (response.has("MRData")) {
                JsonNode mrData = response.get("MRData");
                logger.info("✅ [ErgastService] Found MRData for year {}", year);
                
                if (mrData.has("RaceTable")) {
                    JsonNode raceTable = mrData.get("RaceTable");
                    logger.info("✅ [ErgastService] Found RaceTable for year {}", year);
                    
                    if (raceTable.has("Races")) {
                        JsonNode raceNodes = raceTable.get("Races");
                        logger.info("✅ [ErgastService] Found Races array with {} entries for year {}", raceNodes.size(), year);

                        for (JsonNode raceNode : raceNodes) {
                            try {
                                Map<String, Object> race = new HashMap<>();
                                race.put("round", raceNode.get("round").asInt());
                                race.put("id", raceNode.get("round").asInt());
                                race.put("raceName", raceNode.get("name").asText());
                                race.put("date", raceNode.get("date").asText());

                                // Circuit info
                                JsonNode circuit = raceNode.get("Circuit");
                                if (circuit != null) {
                                    race.put("circuitName", circuit.get("circuitName").asText());
                                    JsonNode location = circuit.get("Location");
                                    if (location != null) {
                                        race.put("location", location.get("country").asText());
                                    }
                                }

                                // Race results (if race has finished)
                                List<Map<String, Object>> results = getRaceResults(year, raceNode.get("round").asInt());
                                race.put("results", results);
                                race.put("hasResults", !results.isEmpty());

                                races.add(race);
                            } catch (Exception e) {
                                logger.warn("⚠️ [ErgastService] Error parsing race node for year {}: {}", year, e.getMessage());
                            }
                        }
                        logger.info("✅ [ErgastService] Successfully parsed {} races for year {}", races.size(), year);
                    } else {
                        logger.error("❌ [ErgastService] Races field not found in RaceTable for year {}. Available fields: {}", year, raceTable.fieldNames());
                    }
                } else {
                    logger.error("❌ [ErgastService] RaceTable not found in MRData for year {}. Available fields: {}", year, mrData.fieldNames());
                }
            } else {
                logger.error("❌ [ErgastService] MRData not found in response for year {}", year);
            }

            // Cache the result only if we got data
            if (!races.isEmpty()) {
                cache.put(cacheKey, races);
                logger.info("✅ [ErgastService] Cached {} races for year {}", races.size(), year);
            } else {
                logger.warn("⚠️ [ErgastService] No races parsed for year {}, NOT caching empty result", year);
            }

        } catch (Exception e) {
            logger.error("❌ [ErgastService] Exception while fetching races for year {}: {} | {}", year, e.getClass().getSimpleName(), e.getMessage(), e);
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
