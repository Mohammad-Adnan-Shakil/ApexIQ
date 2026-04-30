package com.f1pulse.backend.repository;

import com.f1pulse.backend.model.HistoricalSeason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistoricalSeasonRepository extends JpaRepository<HistoricalSeason, Long> {
    Optional<HistoricalSeason> findByYear(Integer year);
}
