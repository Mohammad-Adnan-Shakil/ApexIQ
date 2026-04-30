package com.f1pulse.backend.util;

import com.f1pulse.backend.model.Race;

import java.util.List;

public class StatsUtil {

    public static double calculateAverage(List<Race> races, int n) {
        if (races == null || races.isEmpty()) return 0;

        int limit = Math.min(n, races.size());
        double sum = 0;

        for (int i = 0; i < limit; i++) {
            sum += races.get(i).getPosition();
        }

        return sum / limit;
    }

    public static double calculateStdDev(List<Race> races, int n) {
        if (races == null || races.isEmpty()) return 0;

        int limit = Math.min(n, races.size());

        double mean = calculateAverage(races, n);
        double sum = 0;

        for (int i = 0; i < limit; i++) {
            double diff = races.get(i).getPosition() - mean;
            sum += diff * diff;
        }

        return Math.sqrt(sum / limit);
    }
}
