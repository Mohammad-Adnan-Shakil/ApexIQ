package com.f1pulse.backend.repository;

import com.f1pulse.backend.model.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {
}
