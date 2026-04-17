package com.f1pulse.backend.ai.service;

import com.f1pulse.backend.ai.dto.MultiSimulationRequestDTO;
import com.f1pulse.backend.ai.dto.MultiSimulationResponseDTO;
import com.f1pulse.backend.ai.dto.SimulationRequestDTO;
import com.f1pulse.backend.ai.dto.SimulationResponseDTO;
import com.f1pulse.backend.ai.service.SimulationService;
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

        List<Race> races = raceRepository.findByDriverIdOrderByDateAsc(request.getDriverId());

        List<Integer> historicalPositions = races.stream()
                .map(Race::getPosition)
                .toList();

        List<Integer> combined = new ArrayList<>(historicalPositions);
        combined.addAll(request.getSimulatedPositions());

        double oldAvg = StatsUtil.calculateAverage(historicalPositions);
        double newAvg = StatsUtil.calculateAverage(combined);

        double oldStd = StatsUtil.calculateStdDev(historicalPositions);
        double newStd = StatsUtil.calculateStdDev(combined);

        double oldTrend = StatsUtil.calculateTrend(historicalPositions);
        double newTrend = StatsUtil.calculateTrend(combined);

        MultiSimulationResponseDTO response = new MultiSimulationResponseDTO();

        response.setOldAverage(oldAvg);
        response.setNewAverage(newAvg);
        response.setConsistencyChange(oldStd - newStd);
        response.setTrendChange(newTrend - oldTrend);

        response.setImpactLevel(calculateImpact(oldAvg, newAvg));
        response.setProjectedRankingImpact(calculateRankingImpact(oldAvg, newAvg));

        return response;
    }

    private String calculateImpact(double oldAvg, double newAvg) {
        double diff = oldAvg - newAvg;

        if (diff > 2) return "STRONG IMPROVEMENT";
        if (diff > 1) return "MODERATE IMPROVEMENT";
        return "WEAK IMPROVEMENT";
    }

    private String calculateRankingImpact(double oldAvg, double newAvg) {
        double diff = oldAvg - newAvg;

        if (diff > 2) return "Likely to gain multiple positions";
        if (diff > 1) return "Possible position gain";
        return "Minimal impact";
    }

    @Override
    public SimulationResponseDTO simulate(SimulationRequestDTO request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'simulate'");
    }
}