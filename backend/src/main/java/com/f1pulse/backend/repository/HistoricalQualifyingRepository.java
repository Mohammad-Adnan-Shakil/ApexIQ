package com.f1pulse.backend.repository;

import com.f1pulse.backend.model.HistoricalQualifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistoricalQualifyingRepository extends JpaRepository<HistoricalQualifying, Long> {
    
    Optional<HistoricalQualifying> findByRaceIdAndDriverId(Long raceId, Long driverId);
}
