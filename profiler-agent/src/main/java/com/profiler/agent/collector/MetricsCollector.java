package com.profiler.agent.collector;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton — collects metrics from all methods.
 * Thread-safe.
 */
public class MetricsCollector {
    private static final MetricsCollector INSTANCE = new MetricsCollector();

    private final ConcurrentHashMap<String, MethodMetric> metrics = new ConcurrentHashMap<>();

    private MetricsCollector() {}

    public static MetricsCollector getInstance() {
        return INSTANCE;
    }

    /**
     * Records the execution result of a method.
     */
    public void record(String methodName, long durationNs, long memoryBytes, boolean error) {
        metrics.computeIfAbsent(methodName, MethodMetric::new)
                .record(durationNs, memoryBytes, error);
    }

    public Collection<MethodMetric> getAllMetrics() {
        return metrics.values();
    }

    public MethodMetric getMetric(String methodName) {
        return metrics.get(methodName);
    }

    public void reset() {
        metrics.clear();
    }

    public int size() {
        return metrics.size();
    }
}