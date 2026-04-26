package com.deltabox.backend.repository;

import com.deltabox.backend.model.HistoricalDriverStandings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistoricalDriverStandingsRepository extends JpaRepository<HistoricalDriverStandings, Long> {
    
    Optional<HistoricalDriverStandings> findByYearAndDriverId(Integer year, Long driverId);
}
