package com.f1pulse.backend.ai.service;

import com.f1pulse.backend.ai.dto.MultiSimulationRequestDTO;
import com.f1pulse.backend.ai.dto.MultiSimulationResponseDTO;
import com.f1pulse.backend.ai.dto.SimulationRequestDTO;
import com.f1pulse.backend.ai.dto.SimulationResponseDTO;
import com.f1pulse.backend.ai.util.StatsUtil;
import com.f1pulse.backend.model.Race;
import com.f1pulse.backend.repository.RaceRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SimulationServiceImpl implements SimulationService {

    private final RaceRepository raceRepository;

    public SimulationServiceImpl(RaceRepository raceRepository) {
        this.raceRepository = raceRepository;
    }

    @Override
    public MultiSimulationResponseDTO simulateMultipleRaces(MultiSimulationRequestDTO request) {

        List<Race> races = raceRepository.findByDriverIdAndPositionIsNotNullOrderByDateAsc(request.getDriverId());
        List<Integer> historicalPositions = races.stream().map(Race::getPosition).toList();

        if (historicalPositions.isEmpty()) {
            throw new RuntimeException("No completed races found for simulation");
        }

        List<Integer> combined = new ArrayList<>(historicalPositions);
        combined.addAll(request.getSimulatedPositions());

        double oldAvg = StatsUtil.calculateAverage(historicalPositions);
        double newAvg = StatsUtil.calculateAverage(combined);

        double oldStd = StatsUtil.calculateStdDev(historicalPositions);
        double newStd = StatsUtil.calculateStdDev(combined);

        double oldTrend = StatsUtil.calculateTrend(historicalPositions);
        double newTrend = StatsUtil.calculateTrend(combined);

        MultiSimulationResponseDTO response = new MultiSimulationResponseDTO();

        response.setOldAverage(round2(oldAvg));
        response.setNewAverage(round2(newAvg));
        response.setConsistencyChange(round2(oldStd - newStd));
        response.setTrendChange(round2(newTrend - oldTrend));

        response.setImpactLevel(calculateImpact(oldAvg, newAvg));
        response.setProjectedRankingImpact(calculateRankingImpact(oldAvg, newAvg));

        return response;
    }

    @Override
    public SimulationResponseDTO simulate(SimulationRequestDTO request) {
        List<Race> races = raceRepository.findByDriverIdAndPositionIsNotNullOrderByDateAsc(request.getDriverId());
        List<Integer> historicalPositions = races.stream().map(Race::getPosition).toList();

        if (historicalPositions.isEmpty()) {
            throw new RuntimeException("No completed races found for simulation");
        }

        double oldAverage = StatsUtil.calculateAverage(historicalPositions);

        List<Integer> simulatedPositions = new ArrayList<>(historicalPositions);
        simulatedPositions.add(request.getNewPosition());
        double newAverage = StatsUtil.calculateAverage(simulatedPositions);

        return new SimulationResponseDTO(round2(oldAverage), round2(newAverage), calculateImpact(oldAverage, newAverage));
    }

    private String calculateImpact(double oldAvg, double newAvg) {
        double diff = oldAvg - newAvg;

        if (diff > 2) return "STRONG_IMPROVEMENT";
        if (diff > 1) return "MODERATE_IMPROVEMENT";
        if (diff > 0) return "SLIGHT_IMPROVEMENT";
        if (diff < -1) return "NEGATIVE_IMPACT";
        return "MINIMAL_IMPACT";
    }

    private String calculateRankingImpact(double oldAvg, double newAvg) {
        double diff = oldAvg - newAvg;

        if (diff > 2) return "Likely to gain multiple positions";
        if (diff > 1) return "Likely to gain at least one position";
        if (diff > 0) return "Potential small gain";
        if (diff < -1) return "Likely to lose positions";
        return "Minimal impact";
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
