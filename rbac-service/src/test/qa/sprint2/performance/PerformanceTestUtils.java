package com.example.qa.sprint2.performance;

import java.util.Arrays;

public final class PerformanceTestUtils {

    private PerformanceTestUtils() {
        // Utility class
    }

    public static long percentile(long[] values, double percentile) {

        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Values cannot be empty");
        }

        long[] sortedValues = Arrays.copyOf(values, values.length);
        Arrays.sort(sortedValues);

        int index = (int) Math.ceil(percentile * sortedValues.length) - 1;

        index = Math.max(0, Math.min(index, sortedValues.length - 1));

        return sortedValues[index];
    }

    public static double average(long[] values) {

        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Values cannot be empty");
        }

        long total = 0;

        for (long value : values) {
            total += value;
        }

        return (double) total / values.length;
    }
}