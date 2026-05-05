package com.f1pulse.backend.service;

import com.f1pulse.backend.repository.DriverRepository;
import com.f1pulse.backend.repository.RaceRepository;
import com.f1pulse.backend.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// @Component
// @ConditionalOnProperty(name = "security.enabled", havingValue = "true", matchIfMissing = true)
// public class DataInitializationService implements ApplicationRunner {
//
//     private static final Logger log = LoggerFactory.getLogger(DataInitializationService.class);
//     private final SyncService syncService;
//     private final DriverRepository driverRepository;
//     private final TeamRepository teamRepository;
//     private final RaceRepository raceRepository;
//
//     public DataInitializationService(SyncService syncService, 
//                                     DriverRepository driverRepository,
//                                     TeamRepository teamRepository,
//                                     RaceRepository raceRepository) {
//         this.syncService = syncService;
//         this.driverRepository = driverRepository;
//         this.teamRepository = teamRepository;
//         this.raceRepository = raceRepository;
//     }
//
//     @Override
//     public void run(ApplicationArguments args) {
//         try {
//             // Check if data already exists before seeding
//             long driverCount = driverRepository.count();
//             long teamCount = teamRepository.count();
//             long raceCount = raceRepository.count();
//             
//             log.info("Database check - Drivers: {}, Teams: {}, Races: {}", driverCount, teamCount, raceCount);
//             
//             // Clean up duplicate drivers first (if any)
//             if (driverCount > 0) {
//                 log.info("🧹 Cleaning up duplicate drivers...");
//                 driverRepository.deleteDuplicates();
//                 log.info("✅ Duplicate drivers cleaned up");
//             }
//             
//             // Only seed if database is empty
//             if (driverCount > 0 || teamCount > 0 || raceCount > 0) {
//                 log.info("✅ Database already contains data. Skipping initial sync.");
//                 return;
//             }
//             
//             log.info("🌱 Database is empty. Starting initial sync...");
//             
//             // Sync teams only if empty
//             if (teamRepository.count() == 0) {
//                 syncService.syncTeams();
//                 log.info("✅ Teams synced successfully");
//             } else {
//                 log.info("Teams already exist, skipping team sync");
//             }
//             
//             // Sync drivers only if empty
//             if (driverRepository.count() == 0) {
//                 syncService.syncDrivers();
//                 log.info("✅ Drivers synced successfully");
//             } else {
//                 log.info("Drivers already exist, skipping driver sync");
//             }
//             
//             // Sync races only if empty
//             if (raceRepository.count() == 0) {
//                 syncService.syncRaces();
//                 log.info("✅ Races synced successfully");
//             } else {
//                 log.info("Races already exist, skipping race sync");
//             }
//             
//             syncService.deduplicateScheduleRows(2026);
//             log.info("Initial F1 sync completed");
//         } catch (Exception ex) {
//             log.warn("Initial F1 sync failed: {}", ex.getMessage());
//         }
//     }
// }
