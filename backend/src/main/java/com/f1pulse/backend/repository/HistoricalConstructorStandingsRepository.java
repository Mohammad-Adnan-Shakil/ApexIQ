package com.f1pulse.backend.repository;

import com.f1pulse.backend.model.HistoricalConstructorStandings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistoricalConstructorStandingsRepository extends JpaRepository<HistoricalConstructorStandings, Long> {
    
    Optional<HistoricalConstructorStandings> findByYearAndConstructorId(Integer year, Long constructorId);
}
