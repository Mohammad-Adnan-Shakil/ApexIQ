package com.f1pulse.backend.ai.util;

import java.util.List;

public class StatsUtil {

    public static double calculateAverage(List<Integer> values) {
        return values.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    public static double calculateStdDev(List<Integer> values) {
        double mean = calculateAverage(values);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    public static double calculateTrend(List<Integer> values) {
        int n = values.size();
        if (n < 2) return 0;

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            int x = i;
            int y = values.get(i);

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        return (n * sumXY - sumX * sumY) /
                (n * sumX2 - sumX * sumX + 1e-9);
    }
}