package com.f1pulse.backend.service;

import com.f1pulse.backend.repository.DriverRepository;
import com.f1pulse.backend.repository.RaceRepository;
import com.f1pulse.backend.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializationService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializationService.class);
    private final SyncService syncService;
    private final DriverRepository driverRepository;
    private final TeamRepository teamRepository;
    private final RaceRepository raceRepository;

    public DataInitializationService(SyncService syncService, 
                                    DriverRepository driverRepository,
                                    TeamRepository teamRepository,
                                    RaceRepository raceRepository) {
        this.syncService = syncService;
        this.driverRepository = driverRepository;
        this.teamRepository = teamRepository;
        this.raceRepository = raceRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            // Check if data already exists before seeding
            long driverCount = driverRepository.count();
            long teamCount = teamRepository.count();
            long raceCount = raceRepository.count();
            
            log.info("Database check - Drivers: {}, Teams: {}, Races: {}", driverCount, teamCount, raceCount);
            
            // Only seed if database is empty
            if (driverCount > 0 || teamCount > 0 || raceCount > 0) {
                log.info("✅ Database already contains data. Skipping initial sync.");
                return;
            }
            
            log.info("🌱 Database is empty. Starting initial sync...");
            
            syncService.syncTeams();
            syncService.syncDrivers();
            syncService.syncRaces();
            syncService.deduplicateScheduleRows(2026);
            log.info("Initial F1 sync completed");
        } catch (Exception ex) {
            log.warn("Initial F1 sync failed: {}", ex.getMessage());
        }
    }
}
