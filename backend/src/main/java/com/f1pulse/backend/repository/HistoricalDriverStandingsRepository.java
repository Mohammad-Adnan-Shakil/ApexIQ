package com.f1pulse.backend.repository;

import com.f1pulse.backend.model.HistoricalDriverStandings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistoricalDriverStandingsRepository extends JpaRepository<HistoricalDriverStandings, Long> {
    
    Optional<HistoricalDriverStandings> findByYearAndDriverId(Integer year, Long driverId);
}
