package com.profiler.agent.advice;

import com.profiler.agent.collector.MetricsCollector;
import net.bytebuddy.asm.Advice;

/**
 * Code that gets injected into methods at the bytecode level.
 *
 * IMPORTANT: The code in this class is "inlined" into the target method,
 * so it must be static and dependencies on other classes are limited.
 */
public class TimingAdvice {

    /**
     * Called at the beginning of the method.
     * The returned value is passed to exit via @Advice.Enter.
     */
    @Advice.OnMethodEnter
    public static long enter() {
        return System.nanoTime();
    }

    /**
     * Called at the end of the method (on normal return or exception).
     */
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(@Advice.Origin String method,
                            @Advice.Enter long startTime,
                            @Advice.Thrown Throwable throwable) {
        long duration = System.nanoTime() - startTime;
        boolean error = throwable != null;

        // Record the metric
        MetricsCollector.getInstance().record(method, duration, 0, error);
    }
}