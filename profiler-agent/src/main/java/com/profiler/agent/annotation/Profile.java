package com.profiler.agent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking methods to be profiled.
 *
 * Usage:
 * <pre>
 * {@code
 * @Profile
 * public void doSomething() { ... }
 *
 * @Profile(name = "critical-operation", trackMemory = true)
 * public void importantWork() { ... }
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Profile {
    /**
     * Metric name (default: method name)
     */
    String name() default "";

    /**
     * Whether to track memory allocation
     */
    boolean trackMemory() default false;

    /**
     * Only log if execution exceeds this threshold (in milliseconds)
     */
    long thresholdMs() default 0;
}