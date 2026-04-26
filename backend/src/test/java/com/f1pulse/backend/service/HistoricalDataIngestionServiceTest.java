package com.deltabox.backend.service;

import com.deltabox.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class HistoricalDataIngestionServiceTest {

    @Mock
    private HistoricalSeasonRepository seasonRepository;

    @Mock
    private HistoricalRaceRepository raceRepository;

    @Mock
    private HistoricalDriverRepository driverRepository;

    @Mock
    private HistoricalConstructorRepository constructorRepository;

    @Mock
    private HistoricalResultRepository resultRepository;

    @Mock
    private HistoricalQualifyingRepository qualifyingRepository;

    @Mock
    private HistoricalDriverStandingsRepository driverStandingsRepository;

    @Mock
    private HistoricalConstructorStandingsRepository constructorStandingsRepository;

    @InjectMocks
    private HistoricalDataIngestionService ingestionService;

    @Test
    void getIngestionStatus_ShouldReturnStatus_WhenCalled() {
        // Act
        var status = ingestionService.getIngestionStatus();

        // Assert
        assertThat(status).isNotNull();
        assertThat(status.containsKey("totalRaces")).isTrue();
        assertThat(status.containsKey("totalResults")).isTrue();
        assertThat(status.containsKey("totalDrivers")).isTrue();
        assertThat(status.containsKey("totalConstructors")).isTrue();
        assertThat(status.containsKey("yearsIngested")).isTrue();
        assertThat(status.containsKey("lastUpdated")).isTrue();
    }
}
