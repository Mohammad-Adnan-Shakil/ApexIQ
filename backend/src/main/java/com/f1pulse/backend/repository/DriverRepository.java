package com.f1pulse.backend.repository;

import com.f1pulse.backend.model.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Driver findByCode(String code);
    Page<Driver> findAll(Pageable pageable);
    List<Driver> findBySeasonOrderByPointsDesc(Integer season);
    
    // Find unique drivers by code and season (to prevent duplicates)
    @Query("SELECT DISTINCT d FROM Driver d WHERE d.season = :season ORDER BY d.points DESC")
    List<Driver> findDistinctBySeasonOrderByPointsDesc(Integer season);
    
    // Delete duplicate drivers by code and season, keeping only the latest
    @Query("DELETE FROM Driver d WHERE d.id NOT IN (SELECT MAX(d2.id) FROM Driver d2 WHERE d2.code = d.code AND d2.season = d.season)")
    void deleteDuplicates();
}
