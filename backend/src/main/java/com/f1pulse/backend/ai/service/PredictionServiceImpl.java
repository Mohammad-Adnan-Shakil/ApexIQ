package com.f1pulse.backend.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.f1pulse.backend.ai.dto.PredictionRequestDTO;
import com.f1pulse.backend.ai.dto.PredictionResponseDTO;
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

    @Override
    public PredictionResponseDTO predictRaceOutcome(PredictionRequestDTO request) {
        try {
            String inputJson = objectMapper.writeValueAsString(request);

            logger.info("AI Request: {}", inputJson);

            String output = pythonExecutor.runPredictionScript(inputJson);

            logger.info("AI Response: {}", output);

            if (output == null || output.isEmpty()) {
                throw new RuntimeException("Empty response from AI model");
            }

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
}