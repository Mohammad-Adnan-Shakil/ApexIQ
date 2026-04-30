package com.f1pulse.backend.repository;

import com.f1pulse.backend.model.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Driver findByCode(String code);
    Page<Driver> findAll(Pageable pageable);
    List<Driver> findBySeasonOrderByPointsDesc(Integer season);
}
