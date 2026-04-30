package com.f1pulse.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializationService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializationService.class);
    private final SyncService syncService;

    public DataInitializationService(SyncService syncService) {
        this.syncService = syncService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
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
