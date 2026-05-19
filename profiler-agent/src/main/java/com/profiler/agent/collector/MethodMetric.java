package com.profiler.agent.collector;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Collected statistics for a single method.
 * Thread-safe (uses AtomicLong).
 */
public class MethodMetric {
    private final String methodName;
    private final AtomicLong invocationCount = new AtomicLong(0);
    private final AtomicLong totalTimeNs = new AtomicLong(0);
    private final AtomicLong minTimeNs = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxTimeNs = new AtomicLong(0);
    private final AtomicLong totalMemoryBytes = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);

    public MethodMetric(String methodName) {
        this.methodName = methodName;
    }

    public void record(long durationNs, long memoryBytes, boolean error) {
        invocationCount.incrementAndGet();
        totalTimeNs.addAndGet(durationNs);
        totalMemoryBytes.addAndGet(memoryBytes);

        if (error) {
            errorCount.incrementAndGet();
        }

        // Update Min/Max (CAS loop)
        updateMin(durationNs);
        updateMax(durationNs);
    }

    private void updateMin(long value) {
        long current;
        do {
            current = minTimeNs.get();
            if (value >= current) return;
        } while (!minTimeNs.compareAndSet(current, value));
    }

    private void updateMax(long value) {
        long current;
        do {
            current = maxTimeNs.get();
            if (value <= current) return;
        } while (!maxTimeNs.compareAndSet(current, value));
    }

    // Getters
    public String getMethodName() { return methodName; }
    public long getInvocationCount() { return invocationCount.get(); }
    public long getTotalTimeNs() { return totalTimeNs.get(); }
    public long getMinTimeNs() {
        long min = minTimeNs.get();
        return min == Long.MAX_VALUE ? 0 : min;
    }
    public long getMaxTimeNs() { return maxTimeNs.get(); }
    public long getTotalMemoryBytes() { return totalMemoryBytes.get(); }
    public long getErrorCount() { return errorCount.get(); }

    public double getAvgTimeMs() {
        long count = invocationCount.get();
        if (count == 0) return 0;
        return (totalTimeNs.get() / 1_000_000.0) / count;
    }
}