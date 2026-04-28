package com.deltabox.backend.config;

import com.deltabox.backend.repository.DriverRepository;
import com.deltabox.backend.repository.RaceRepository;
import com.deltabox.backend.repository.TeamRepository;
import com.deltabox.backend.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * DatabaseSeeder runs on application startup to seed initial data.
 * It checks if the database is empty and only seeds if necessary.
 * This ensures seeding only runs once on first deployment to Railway.
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final DriverRepository driverRepository;
    private final TeamRepository teamRepository;
    private final RaceRepository raceRepository;
    private final SyncService syncService;
    private final Environment environment;

    public DatabaseSeeder(DriverRepository driverRepository,
                          TeamRepository teamRepository,
                          RaceRepository raceRepository,
                          SyncService syncService,
                          Environment environment) {
        this.driverRepository = driverRepository;
        this.teamRepository = teamRepository;
        this.raceRepository = raceRepository;
        this.syncService = syncService;
        this.environment = environment;
    }

    @Override
    public void run(String... args) throws Exception {
        // Skip seeding if explicitly disabled via environment variable
        boolean skipSeeding = Boolean.parseBoolean(environment.getProperty("SKIP_DB_SEEDING", "false"));
        if (skipSeeding) {
            log.info("⏭️ Database seeding skipped (SKIP_DB_SEEDING=true)");
            return;
        }

        // Check if database is already seeded
        boolean isSeeded = isDatabaseSeeded();
        
        if (isSeeded) {
            log.info("✅ Database already contains data. Skipping initial seeding.");
            return;
        }

        log.info("🌱 Database is empty. Starting initial seeding...");
        
        try {
            // Seed teams first (drivers depend on teams)
            log.info("Seeding teams...");
            syncService.syncTeams();
            log.info("✅ Teams seeded successfully");

            // Seed drivers
            log.info("Seeding drivers...");
            syncService.syncDrivers();
            log.info("✅ Drivers seeded successfully");

            // Seed races
            log.info("Seeding races...");
            syncService.syncRaces();
            log.info("✅ Races seeded successfully");

            log.info("🎉 Initial database seeding completed successfully!");
        } catch (Exception e) {
            log.error("❌ Error during initial database seeding", e);
            // Don't throw - allow application to start even if seeding fails
            // Admin can manually trigger seeding via AdminIngestionController
        }
    }

    /**
     * Check if the database is already seeded by checking if any data exists
     */
    private boolean isDatabaseSeeded() {
        long driverCount = driverRepository.count();
        long teamCount = teamRepository.count();
        long raceCount = raceRepository.count();
        
        log.info("Database status - Drivers: {}, Teams: {}, Races: {}", driverCount, teamCount, raceCount);
        
        // Consider database seeded if it has at least some data
        return driverCount > 0 || teamCount > 0 || raceCount > 0;
    }
}
