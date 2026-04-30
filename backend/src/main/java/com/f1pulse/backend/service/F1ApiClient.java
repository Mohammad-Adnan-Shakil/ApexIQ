package com.f1pulse.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.f1pulse.backend.model.DriverDTO;
import com.f1pulse.backend.model.RaceDTO;
import com.f1pulse.backend.model.RaceResultDTO;
import com.f1pulse.backend.model.TeamDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class F1ApiClient {

    private static final String BASE_URL = "https://api.jolpi.ca/ergast/f1";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public F1ApiClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<DriverDTO> fetchDrivers() {
        try {
            String response = restTemplate.getForObject(BASE_URL + "/current/driverStandings.json", String.class);
            JsonNode standings = objectMapper.readTree(response)
                    .path("MRData")
                    .path("StandingsTable")
                    .path("StandingsLists");

            List<DriverDTO> result = new ArrayList<>();
            if (!standings.isArray() || standings.isEmpty()) {
                return result;
            }

            JsonNode driverStandings = standings.get(0).path("DriverStandings");
            for (JsonNode standing : driverStandings) {
                JsonNode driver = standing.path("Driver");
                JsonNode constructors = standing.path("Constructors");

                String name = driver.path("givenName").asText() + " " + driver.path("familyName").asText();
                String code = driver.path("code").asText(driver.path("driverId").asText("UNK")).toUpperCase();
                String nationality = driver.path("nationality").asText();
                String team = constructors.isArray() && !constructors.isEmpty()
                        ? constructors.get(0).path("name").asText("")
                        : "";
                double points = standing.path("points").asDouble(0.0);

                result.add(new DriverDTO(name, code, nationality, team, points));
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching drivers: " + e.getMessage(), e);
        }
    }

    public List<TeamDTO> fetchTeams() {
        try {
            String response = restTemplate.getForObject(BASE_URL + "/current/constructors.json", String.class);
            JsonNode teams = objectMapper.readTree(response)
                    .path("MRData")
                    .path("ConstructorTable")
                    .path("Constructors");

            List<TeamDTO> result = new ArrayList<>();

            for (JsonNode team : teams) {
                result.add(new TeamDTO(
                        team.path("name").asText(),
                        team.path("nationality").asText()
                ));
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error fetching teams: " + e.getMessage(), e);
        }
    }

    public List<RaceDTO> fetchRaces() {
        try {
            String response = restTemplate.getForObject(BASE_URL + "/current/races.json", String.class);
            JsonNode races = objectMapper.readTree(response)
                    .path("MRData")
                    .path("RaceTable")
                    .path("Races");

            List<RaceDTO> result = new ArrayList<>();

            for (JsonNode race : races) {
                JsonNode circuit = race.path("Circuit");
                JsonNode locationNode = circuit.path("Location");

                result.add(new RaceDTO(
                        race.path("round").asInt(),
                        race.path("raceName").asText(),
                        circuit.path("circuitName").asText(),
                        locationNode.path("locality").asText(),
                        locationNode.path("country").asText(),
                        race.path("date").asText()
                ));
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching races: " + e.getMessage(), e);
        }
    }

    public List<RaceResultDTO> fetchRaceResults() {
        try {
            String response = restTemplate.getForObject(BASE_URL + "/current/results.json?limit=2000", String.class);
            JsonNode races = objectMapper.readTree(response)
                    .path("MRData")
                    .path("RaceTable")
                    .path("Races");

            List<RaceResultDTO> results = new ArrayList<>();

            for (JsonNode race : races) {
                Integer round = race.path("round").asInt();
                String raceName = race.path("raceName").asText();
                String date = race.path("date").asText();
                JsonNode circuit = race.path("Circuit");
                String circuitName = circuit.path("circuitName").asText();
                JsonNode locationNode = circuit.path("Location");
                String location = locationNode.path("locality").asText();
                String country = locationNode.path("country").asText();

                JsonNode raceResults = race.path("Results");
                for (JsonNode row : raceResults) {
                    JsonNode driver = row.path("Driver");
                    String driverCode = driver.path("code").asText(driver.path("driverId").asText("")).toUpperCase();
                    Integer position = row.path("position").asInt();

                    results.add(new RaceResultDTO(
                            round,
                            raceName,
                            circuitName,
                            location,
                            country,
                            date,
                            driverCode,
                            position
                    ));
                }
            }

            return results;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching race results: " + e.getMessage(), e);
        }
    }
}
