package com.f1pulse.backend.ai.service;

import com.f1pulse.backend.ai.dto.DriverInsightsResponseDTO;
import com.f1pulse.backend.model.Race;
import com.f1pulse.backend.repository.RaceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverInsightsServiceImpl implements DriverInsightsService {

    private final RaceRepository raceRepository;

    public DriverInsightsServiceImpl(RaceRepository raceRepository) {
        this.raceRepository = raceRepository;
    }

    @Override
    public DriverInsightsResponseDTO getDriverInsights(Long driverId) {

        List<Race> races = raceRepository.findTop10ByDriverIdAndPositionIsNotNullOrderByDateDesc(driverId);

        if (races.isEmpty()) {
            throw new RuntimeException("No completed race data found for driver");
        }

        List<Race> recent = races.stream().limit(5).toList();

        List<Integer> positions = recent.stream()
                .map(Race::getPosition)
                .toList();

        double avg = positions.stream()
                .mapToInt(i -> i)
                .average()
                .orElse(0);

        int best = positions.stream()
                .min(Integer::compare)
                .orElse(0);

        int worst = positions.stream()
                .max(Integer::compare)
                .orElse(0);

        double variance = positions.stream()
                .mapToDouble(p -> Math.pow(p - avg, 2))
                .average()
                .orElse(0);

        double consistencyScore = Math.max(0, Math.min(10, 10 - variance));

        int first = positions.get(0);
        int last = positions.get(positions.size() - 1);

        String trend;
        if (last < first) trend = "IMPROVING";
        else if (last > first) trend = "DECLINING";
        else trend = "STABLE";

        String rating;
        if (avg <= 3) rating = "ELITE";
        else if (avg <= 6) rating = "STRONG";
        else if (avg <= 10) rating = "AVERAGE";
        else rating = "WEAK";

        return new DriverInsightsResponseDTO(
                avg,
                best,
                worst,
                consistencyScore,
                trend,
                rating
        );
    }
}
