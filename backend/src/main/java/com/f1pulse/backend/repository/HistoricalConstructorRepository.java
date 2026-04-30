package com.f1pulse.backend.repository;

import com.f1pulse.backend.model.HistoricalConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistoricalConstructorRepository extends JpaRepository<HistoricalConstructor, Long> {
    Optional<HistoricalConstructor> findByConstructorRef(String constructorRef);
}
