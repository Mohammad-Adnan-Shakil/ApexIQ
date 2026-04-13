package com.f1pulse.backend.service;

import com.f1pulse.backend.model.*;
import com.f1pulse.backend.repository.*;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Service
public class SyncService {

    private final DriverRepository driverRepository;
    private final TeamRepository teamRepository;
    private final RaceRepository raceRepository;
    private final SyncMetaRepository syncMetaRepository;
    private final F1ApiClient f1ApiClient;
    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    private final long CACHE_DURATION = 60 * 60 * 1000; // 1 hour

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

    // ================= COMMON METHOD =================

    private boolean shouldSync(String key) {
        SyncMeta meta = syncMetaRepository.findById(key).orElse(null);
        long currentTime = System.currentTimeMillis();

        return meta == null || (currentTime - meta.getLastSyncTime()) >= CACHE_DURATION;
    }

    private void updateSyncTime(String key) {
        syncMetaRepository.save(new SyncMeta(key, System.currentTimeMillis()));
    }

    // ================= DRIVERS =================

    public List<Driver> syncDrivers() {
        try {
            String key = "drivers";

            if (!shouldSync(key)) {
                log.info("Using cached drivers data");
                return driverRepository.findAll();
            }

            log.info("Syncing fresh drivers data");

            List<DriverDTO> dtos = f1ApiClient.fetchDrivers();
            List<Driver> drivers = new ArrayList<>();

            for (DriverDTO dto : dtos) {

                Driver existing = driverRepository.findByCode(dto.getCode());

                if (existing != null) {
                    existing.setName(dto.getName());
                    existing.setNationality(dto.getNationality());
                    drivers.add(existing);
                } else {
                    drivers.add(new Driver(
                            dto.getName(),
                            dto.getCode(),
                            dto.getNationality()));
                }
            }

            updateSyncTime(key);

            return driverRepository.saveAll(drivers);

        } catch (Exception e) {
            log.error("Error syncing drivers: {}", e.getMessage());
            throw e;
        }
    }

    public List<Team> syncTeams() {
        try {
            String key = "teams";

            if (!shouldSync(key)) {
                System.out.println("Using cached teams data");
                return teamRepository.findAll();
            }

            System.out.println("Syncing fresh teams data");

            List<TeamDTO> dtos = f1ApiClient.fetchTeams();
            List<Team> teams = new ArrayList<>();

            for (TeamDTO dto : dtos) {

                Team existing = teamRepository.findByName(dto.getName());

                if (existing != null) {
                    existing.setNationality(dto.getNationality());
                    teams.add(existing);
                } else {
                    teams.add(new Team(
                            dto.getName(),
                            dto.getNationality()));
                }
            }

            updateSyncTime(key);

            return teamRepository.saveAll(teams);
        } catch (Exception e) {
            log.error("Error syncing drivers: {}", e.getMessage());
            throw e;
        }
    }

    // ================= RACES =================

    public List<Race> syncRaces() {
        try {

            String key = "races";

            if (!shouldSync(key)) {
                System.out.println("Using cached races data");
                return raceRepository.findAll();
            }

            System.out.println("Syncing fresh races data");

            List<RaceDTO> dtos = f1ApiClient.fetchRaces();
            List<Race> races = new ArrayList<>();

            for (RaceDTO dto : dtos) {

                Driver driver = driverRepository.findAll().get(0); // temporary mapping

                races.add(new Race(
                        driver,
                        dto.getRaceName(),
                        dto.getCircuitName(),
                        dto.getLocation(),
                        dto.getCountry(),
                        dto.getDate(),
                        new java.util.Random().nextInt(20) + 1));
            }

            updateSyncTime(key);

            return raceRepository.saveAll(races);
        } catch (Exception e) {
            log.error("Error syncing drivers: {}", e.getMessage());
            throw e;
        }
    }
}