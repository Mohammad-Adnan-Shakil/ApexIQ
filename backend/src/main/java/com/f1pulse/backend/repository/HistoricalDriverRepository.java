package com.f1pulse.backend.repository;

import com.f1pulse.backend.model.HistoricalDriver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistoricalDriverRepository extends JpaRepository<HistoricalDriver, Long> {
    Optional<HistoricalDriver> findByDriverRef(String driverRef);
    Optional<HistoricalDriver> findByCode(String code);
}
