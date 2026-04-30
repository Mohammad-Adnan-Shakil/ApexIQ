package com.f1pulse.backend.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.f1pulse.backend.exception.PythonExecutionException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class PythonExecutor {

    private static final long EXEC_TIMEOUT_SECONDS = 30L;

    private final ObjectMapper objectMapper;

    public PythonExecutor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode runScript(String scriptPath, Map<String, Object> payload) {
        Path resolvedScript = resolveScriptPath(scriptPath);
        String inputJson;
        try {
            inputJson = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new PythonExecutionException("Failed to serialize ML payload", e);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(resolvePythonExecutable(), resolvedScript.toString());

        try {
            Process process = processBuilder.start();
            writeInput(process, inputJson);

            boolean finished = process.waitFor(EXEC_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new PythonExecutionException("ML process timed out after " + EXEC_TIMEOUT_SECONDS + " seconds");
            }

            String stdout = readStream(process.getInputStream());
            String stderr = readStream(process.getErrorStream());
            int exitCode = process.exitValue();

            if (exitCode != 0) {
                throw new PythonExecutionException(
                        String.format("ML process failed (exit=%d): %s", exitCode, sanitize(stderr))
                );
            }

            if (stdout == null || stdout.isBlank()) {
                throw new PythonExecutionException("ML process returned empty output");
            }

            JsonNode parsed = objectMapper.readTree(stdout);
            if (parsed.hasNonNull("error")) {
                throw new PythonExecutionException("ML model error: " + parsed.get("error").asText());
            }
            return parsed;

        } catch (PythonExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new PythonExecutionException("Failed to execute ML script: " + e.getMessage(), e);
        }
    }

    private static String resolvePythonExecutable() {
        String envPython = System.getenv("PYTHON_EXECUTABLE");
        if (envPython != null && !envPython.isBlank()) {
            return envPython;
        }
        return "python";
    }

    private static void writeInput(Process process, String inputJson) throws IOException {
        try (Writer writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(inputJson);
            writer.flush();
        }
    }

    private static String readStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!builder.isEmpty()) {
                    builder.append('\n');
                }
                builder.append(line);
            }
            return builder.toString();
        }
    }

    private static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ");
    }

    private static Path resolveScriptPath(String scriptPath) {
        List<Path> candidates = List.of(
                Path.of(scriptPath),
                Path.of("backend").resolve(scriptPath),
                Path.of(scriptPath.startsWith("backend/") ? scriptPath.substring("backend/".length()) : scriptPath)
        );

        for (Path candidate : candidates) {
            Path absolute = candidate.toAbsolutePath().normalize();
            if (Files.exists(absolute) && Files.isRegularFile(absolute)) {
                return absolute;
            }
        }

        throw new PythonExecutionException("ML script not found: " + scriptPath);
    }
}
