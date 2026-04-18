package com.f1pulse.backend.util;

import java.io.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PythonExecutor {

   public static JsonNode runScript(String scriptPath, String jsonInput) {
    try {
        ProcessBuilder pb = new ProcessBuilder("python", scriptPath);

        Process process = pb.start();

        // 🔥 Send JSON via STDIN
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream())
        );
        writer.write(jsonInput);
        writer.flush();
        writer.close();

        // 🔥 Read output
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        );

        String output = reader.readLine();

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(output);

    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}
}