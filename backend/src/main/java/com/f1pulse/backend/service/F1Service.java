package com.f1pulse.backend.service;

import com.f1pulse.backend.model.*;
import com.f1pulse.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class F1Service {

    private final DriverRepository driverRepository;
    private final TeamRepository teamRepository;
    private final RaceRepository raceRepository;
    private final F1ApiClient f1ApiClient;
    private final SyncService syncService;

    public F1Service(DriverRepository driverRepository,
                     TeamRepository teamRepository,
                     RaceRepository raceRepository,
                     F1ApiClient f1ApiClient,
                     SyncService syncService) {

        this.driverRepository = driverRepository;
        this.teamRepository = teamRepository;
        this.raceRepository = raceRepository;
        this.f1ApiClient = f1ApiClient;
        this.syncService = syncService;
    }

    // ================= DRIVERS =================

    public List<DriverDTO> getDrivers() {
        return f1ApiClient.fetchDrivers();
    }

    public List<Driver> saveDrivers() {
        return syncService.syncDrivers();
    }

    public List<Driver> getDriversFromDB() {
        return driverRepository.findAll();
    }

    public Page<Driver> getDriversPaginated(Pageable pageable) {
    return driverRepository.findAll(pageable);
}

    // ================= TEAMS =================

    public List<TeamDTO> getTeams() {
        return f1ApiClient.fetchTeams();
    }

    public List<Team> saveTeams() {
        return syncService.syncTeams();
    }

    public List<Team> getTeamsFromDB() {
        return teamRepository.findAll();
    }

    // ================= RACES =================

    public List<RaceDTO> getRaces() {
        return f1ApiClient.fetchRaces();
    }

    public List<Race> saveRaces() {
        return syncService.syncRaces();
    }

    public List<Race> getRacesFromDB() {
        return raceRepository.findAll();
    }
}
