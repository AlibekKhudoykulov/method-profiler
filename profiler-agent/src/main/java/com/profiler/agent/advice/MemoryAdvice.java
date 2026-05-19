package com.profiler.agent.advice;

import com.profiler.agent.collector.MetricsCollector;
import net.bytebuddy.asm.Advice;

/**
 * Advice that tracks memory allocation.
 * Only applied to methods annotated with @Profile(trackMemory=true).
 */
public class MemoryAdvice {

    @Advice.OnMethodEnter
    public static long[] enter() {
        Runtime rt = Runtime.getRuntime();
        long usedMemory = rt.totalMemory() - rt.freeMemory();
        return new long[]{System.nanoTime(), usedMemory};
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(@Advice.Origin String method,
                            @Advice.Enter long[] enterData,
                            @Advice.Thrown Throwable throwable) {
        long duration = System.nanoTime() - enterData[0];

        Runtime rt = Runtime.getRuntime();
        long usedMemoryAfter = rt.totalMemory() - rt.freeMemory();
        long memoryDelta = Math.max(0, usedMemoryAfter - enterData[1]);

        boolean error = throwable != null;
        MetricsCollector.getInstance().record(method, duration, memoryDelta, error);
    }
}