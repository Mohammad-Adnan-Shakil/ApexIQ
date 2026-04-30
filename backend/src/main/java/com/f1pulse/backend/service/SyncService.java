package com.f1pulse.backend.service;

import com.f1pulse.backend.model.Driver;
import com.f1pulse.backend.model.DriverDTO;
import com.f1pulse.backend.model.Race;
import com.f1pulse.backend.model.RaceDTO;
import com.f1pulse.backend.model.RaceResultDTO;
import com.f1pulse.backend.model.SyncMeta;
import com.f1pulse.backend.model.Team;
import com.f1pulse.backend.model.TeamDTO;
import com.f1pulse.backend.repository.DriverRepository;
import com.f1pulse.backend.repository.RaceRepository;
import com.f1pulse.backend.repository.SyncMetaRepository;
import com.f1pulse.backend.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncService.class);
    private static final int CURRENT_SEASON = 2026;
    private static final long CACHE_DURATION = 60 * 60 * 1000L;

    private final DriverRepository driverRepository;
    private final TeamRepository teamRepository;
    private final RaceRepository raceRepository;
    private final SyncMetaRepository syncMetaRepository;
    private final F1ApiClient f1ApiClient;

    public SyncService(DriverRepository driverRepository,
                       TeamRepository teamRepository,
                       RaceRepository raceRepository,
                       SyncMetaRepository syncMetaRepository,
                       F1ApiClient f1ApiClient) {

        this.driverRepository = driverRepository;
        this.teamRepository = teamRepository;
        this.raceRepository = raceRepository;
        this.syncMetaRepository = syncMetaRepository;
        this.f1ApiClient = f1ApiClient;
    }

    private boolean shouldSync(String key) {
        SyncMeta meta = syncMetaRepository.findById(key).orElse(null);
        long currentTime = System.currentTimeMillis();
        return meta == null || (currentTime - meta.getLastSyncTime()) >= CACHE_DURATION;
    }

    private void updateSyncTime(String key) {
        syncMetaRepository.save(new SyncMeta(key, System.currentTimeMillis()));
    }

    public List<Driver> syncDrivers() {
        try {
            String key = "drivers";
            if (!shouldSync(key)) {
                log.info("Using cached drivers data");
                return driverRepository.findBySeasonOrderByPointsDesc(CURRENT_SEASON);
            }

            log.info("Syncing fresh drivers data");
            List<DriverDTO> dtos = f1ApiClient.fetchDrivers();
            Map<String, Team> teamsByName = teamRepository.findAll().stream()
                    .collect(Collectors.toMap(Team::getName, t -> t, (a, b) -> a));

            List<Driver> drivers = new ArrayList<>();
            for (DriverDTO dto : dtos) {
                Driver driver = driverRepository.findByCode(dto.getCode());
                if (driver == null) {
                    driver = new Driver(dto.getCode(), dto.getName(), dto.getNationality());
                }

                driver.setName(dto.getName());
                driver.setNationality(dto.getNationality());
                driver.setTeam(dto.getTeam());
                driver.setPoints(dto.getPoints() == null ? 0.0 : dto.getPoints());
                driver.setSeason(CURRENT_SEASON);

                Team mappedTeam = teamsByName.get(dto.getTeam());
                if (mappedTeam != null) {
                    driver.setTeamId(mappedTeam.getId());
                }

                drivers.add(driver);
            }

            updateSyncTime(key);
            return driverRepository.saveAll(drivers);

        } catch (Exception e) {
            log.error("Error syncing drivers", e);
            throw e;
        }
    }

    public List<Team> syncTeams() {
        try {
            String key = "teams";
            if (!shouldSync(key)) {
                log.info("Using cached teams data");
                return teamRepository.findAll();
            }

            log.info("Syncing fresh teams data");
            List<TeamDTO> dtos = f1ApiClient.fetchTeams();
            List<Team> teams = new ArrayList<>();

            for (TeamDTO dto : dtos) {
                Team team = teamRepository.findByName(dto.getName());
                if (team == null) {
                    team = new Team(dto.getName(), dto.getNationality());
                } else {
                    team.setNationality(dto.getNationality());
                }
                teams.add(team);
            }

            updateSyncTime(key);
            return teamRepository.saveAll(teams);

        } catch (Exception e) {
            log.error("Error syncing teams", e);
            throw e;
        }
    }

    public List<Race> syncRaces() {
        try {
            String key = "races";
            if (!shouldSync(key)) {
                log.info("Using cached races data");
                deduplicateScheduleRows(CURRENT_SEASON);
                return raceRepository.findBySeasonAndDriverIdIsNullOrderByDateAsc(CURRENT_SEASON);
            }

            log.info("Syncing fresh races data");
            List<RaceDTO> calendar = f1ApiClient.fetchRaces();
            List<RaceResultDTO> results = f1ApiClient.fetchRaceResults();

            Map<Integer, List<RaceResultDTO>> resultsByRound = results.stream()
                    .collect(Collectors.groupingBy(RaceResultDTO::getRound));

            Map<String, Driver> driversByCode = new HashMap<>();
            for (Driver driver : driverRepository.findAll()) {
                if (driver.getCode() != null) {
                    driversByCode.put(driver.getCode().toUpperCase(), driver);
                }
            }

            List<Race> rowsToPersist = new ArrayList<>();

            LocalDate today = LocalDate.now();
            for (RaceDTO raceDto : calendar) {
                LocalDate raceDate = parseDateOrMin(raceDto.getDate());
                List<RaceResultDTO> completedRows = resultsByRound.getOrDefault(raceDto.getRound(), List.of());
                boolean completed = !completedRows.isEmpty() && !raceDate.isAfter(today);

                Race scheduleRow = new Race(
                        null,
                        raceDto.getRaceName(),
                        raceDto.getCircuitName(),
                        raceDto.getLocation(),
                        raceDto.getCountry(),
                        raceDto.getDate(),
                        null
                );
                scheduleRow.setSeason(CURRENT_SEASON);
                scheduleRow.setRound(raceDto.getRound());
                scheduleRow.setStatus(completed ? "COMPLETED" : "SCHEDULED");
                rowsToPersist.add(scheduleRow);

                if (completed) {
                    for (RaceResultDTO result : completedRows) {
                        Driver driver = driversByCode.get(result.getDriverCode());
                        if (driver == null) {
                            continue;
                        }

                        Race resultRow = new Race(
                                driver.getId(),
                                result.getRaceName(),
                                result.getCircuitName(),
                                result.getLocation(),
                                result.getCountry(),
                                result.getDate(),
                                result.getPosition()
                        );
                        resultRow.setSeason(CURRENT_SEASON);
                        resultRow.setRound(result.getRound());
                        resultRow.setStatus("COMPLETED");
                        rowsToPersist.add(resultRow);
                    }
                }
            }

            raceRepository.deleteAllInBatch();
            List<Race> saved = raceRepository.saveAll(rowsToPersist);
            deduplicateScheduleRows(CURRENT_SEASON);
            updateSyncTime(key);
            return saved.stream()
                    .filter(r -> r.getDriverId() == null)
                    .sorted(Comparator.comparing(r -> Objects.requireNonNullElse(r.getRound(), Integer.MAX_VALUE)))
                    .toList();

        } catch (Exception e) {
            log.error("Error syncing races", e);
            throw e;
        }
    }

    public void deduplicateScheduleRows(int season) {
        List<Race> scheduleRows = raceRepository.findBySeasonAndDriverIdIsNull(season);
        if (scheduleRows.isEmpty()) {
            return;
        }

        Map<String, Race> unique = new LinkedHashMap<>();
        for (Race race : scheduleRows) {
            String key = race.getRound() != null
                    ? "round-" + race.getRound()
                    : "fallback-" + race.getRaceName() + "-" + race.getDate();
            unique.putIfAbsent(key, race);
        }

        List<Race> duplicates = scheduleRows.stream()
                .filter(race -> unique.values().stream().noneMatch(kept -> kept.getId().equals(race.getId())))
                .toList();

        if (!duplicates.isEmpty()) {
            raceRepository.deleteAllInBatch(duplicates);
            log.info("Removed {} duplicate race schedule rows", duplicates.size());
        }
    }

    private static LocalDate parseDateOrMin(String date) {
        try {
            return LocalDate.parse(date);
        } catch (Exception ex) {
            return LocalDate.MIN;
        }
    }
}
