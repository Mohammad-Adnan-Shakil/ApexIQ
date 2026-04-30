package com.f1pulse.backend.ai.service;

import com.f1pulse.backend.ai.dto.MultiSimulationRequestDTO;
import com.f1pulse.backend.ai.dto.MultiSimulationResponseDTO;
import com.f1pulse.backend.ai.dto.SimulationRequestDTO;
import com.f1pulse.backend.ai.dto.SimulationResponseDTO;

public interface SimulationService {

    MultiSimulationResponseDTO simulateMultipleRaces(MultiSimulationRequestDTO request);

    SimulationResponseDTO simulate(SimulationRequestDTO request);
}
