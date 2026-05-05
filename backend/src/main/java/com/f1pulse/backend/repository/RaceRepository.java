package com.f1pulse.backend.repository;

import com.f1pulse.backend.model.Race;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RaceRepository extends JpaRepository<Race, Long> {

    List<Race> findTop10ByDriverIdAndPositionIsNotNullOrderByDateDesc(Long driverId);

    List<Race> findByDriverIdAndPositionIsNotNullOrderByDateAsc(Long driverId);

    List<Race> findBySeasonAndDriverIdIsNullOrderByDateAsc(Integer season);

    List<Race> findBySeasonAndDriverIdIsNull(Integer season);
    
    List<Race> findByRaceId(Long raceId);
}
