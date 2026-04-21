package com.f1pulse.backend.controller;

import com.f1pulse.backend.model.Race;
import com.f1pulse.backend.repository.RaceRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/races")
@Tag(name = "Races", description = "F1 race schedule and results")
public class RaceController {

    private final RaceRepository raceRepository;

    public RaceController(RaceRepository raceRepository) {
        this.raceRepository = raceRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllRaces() {
        try {
            List<Race> races = raceRepository.findBySeasonAndDriverIdIsNullOrderByDateAsc(2026);

            // Defensive dedupe: one schedule row per round.
            Map<String, Race> uniqueByRound = new LinkedHashMap<>();
            for (Race race : races) {
                String key = race.getRound() != null
                        ? "round-" + race.getRound()
                        : "fallback-" + race.getRaceName() + "-" + race.getDate();
                uniqueByRound.putIfAbsent(key, race);
            }

            List<Race> cleaned = uniqueByRound.values().stream()
                    .sorted(
                            Comparator.comparing((Race r) -> Objects.requireNonNullElse(r.getRound(), Integer.MAX_VALUE))
                                    .thenComparing(Race::getDate, Comparator.nullsLast(String::compareTo))
                    )
                    .peek(race -> race.setStatus(resolveRaceStatus(race.getDate())))
                    .toList();

            return ResponseEntity.ok(cleaned);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to load races");
        }
    }

    private static String resolveRaceStatus(String raceDate) {
        try {
            LocalDate parsed = LocalDate.parse(raceDate);
            return parsed.isAfter(LocalDate.now()) ? "SCHEDULED" : "COMPLETED";
        } catch (Exception ex) {
            return "SCHEDULED";
        }
    }
}
