package com.f1pulse.backend.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.f1pulse.backend.ai.dto.PredictionRequestDTO;
import com.f1pulse.backend.ai.dto.PredictionResponseDTO;
import com.f1pulse.backend.ai.dto.DriverInsightsResponseDTO;
import com.f1pulse.backend.ai.integration.PythonExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PredictionServiceImpl implements PredictionService {

    private static final Logger logger = LoggerFactory.getLogger(PredictionServiceImpl.class);

    private final PythonExecutor pythonExecutor;
    private final ObjectMapper objectMapper;

    public PredictionServiceImpl(PythonExecutor pythonExecutor, ObjectMapper objectMapper) {
        this.pythonExecutor = pythonExecutor;
        this.objectMapper = objectMapper;
    }

    // =========================
    // 🔥 PHASE 1 (AI PREDICTION)
    // =========================
    @Override
    public PredictionResponseDTO predictRaceOutcome(PredictionRequestDTO request) {
        try {
            String inputJson = objectMapper.writeValueAsString(request);

            logger.info("AI Request: {}", inputJson);

            String output = pythonExecutor.runPredictionScript(inputJson);

            logger.info("AI Response: {}", output);

            if (output.contains("error")) {
                logger.error("Python returned error: {}", output);
                throw new RuntimeException("AI error: " + output);
            }

            return objectMapper.readValue(output, PredictionResponseDTO.class);

        } catch (Exception e) {
            logger.error("AI prediction failed", e);
            throw new RuntimeException("AI prediction failed: " + e.getMessage(), e);
        }
    }

    // =========================
    // 🚀 PHASE 2 (INSIGHTS)
    // =========================
    @Override
    public DriverInsightsResponseDTO getDriverInsights(Long driverId) {

        // 🔥 Mock data (we’ll replace with DB later)
        int[] lastPositions = {5, 6, 4, 7, 3};

        double avg = 0;
        int best = Integer.MAX_VALUE;
        int worst = Integer.MIN_VALUE;

        for (int pos : lastPositions) {
            avg += pos;
            best = Math.min(best, pos);
            worst = Math.max(worst, pos);
        }

        avg /= lastPositions.length;

        // variance → consistency
        double variance = 0;
        for (int pos : lastPositions) {
            variance += Math.pow(pos - avg, 2);
        }
        variance /= lastPositions.length;

        double consistency = 10 - variance;

        String trend = lastPositions[lastPositions.length - 1] < lastPositions[0]
                ? "IMPROVING"
                : "DECLINING";

        DriverInsightsResponseDTO dto = new DriverInsightsResponseDTO();
        dto.setAvgPosition(avg);
        dto.setBestFinish(best);
        dto.setWorstFinish(worst);
        dto.setConsistencyScore(Math.max(0, consistency));
        dto.setFormTrend(trend);

        return dto;
    }
}