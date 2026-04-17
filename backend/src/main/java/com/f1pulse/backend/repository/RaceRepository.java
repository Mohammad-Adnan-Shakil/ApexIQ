package com.f1pulse.backend.repository;

import com.f1pulse.backend.model.Race;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RaceRepository extends JpaRepository<Race, Long> {
    @Query("SELECT r FROM Race r WHERE r.driverId = :driverId ORDER BY r.date DESC")
    List<Race> findRecentRacesByDriver(Long driverId);

    List<Race> findByDriverIdOrderByDateAsc(Long driverId);
}