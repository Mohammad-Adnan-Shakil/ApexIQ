package com.f1pulse.backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SyncScheduler {

    private final SyncService syncService;

    public SyncScheduler(SyncService syncService) {
        this.syncService = syncService;
    }

    @Scheduled(fixedDelayString = "3600000")
    public void syncAllAutomatically() {
        syncService.syncTeams();
        syncService.syncDrivers();
        syncService.syncRaces();
        syncService.deduplicateScheduleRows(2026);
    }
}
