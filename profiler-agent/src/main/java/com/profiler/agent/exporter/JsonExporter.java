package com.profiler.agent.exporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.profiler.agent.collector.MethodMetric;
import com.profiler.agent.collector.MetricsCollector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exporter that converts metrics to JSON format.
 */
public class JsonExporter {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String exportAll() throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();

        for (MethodMetric metric : MetricsCollector.getInstance().getAllMetrics()) {
            Map<String, Object> data = new HashMap<>();
            data.put("method", metric.getMethodName());
            data.put("invocations", metric.getInvocationCount());
            data.put("totalTimeMs", metric.getTotalTimeNs() / 1_000_000.0);
            data.put("avgTimeMs", metric.getAvgTimeMs());
            data.put("minTimeMs", metric.getMinTimeNs() / 1_000_000.0);
            data.put("maxTimeMs", metric.getMaxTimeNs() / 1_000_000.0);
            data.put("totalMemoryKB", metric.getTotalMemoryBytes() / 1024.0);
            data.put("errors", metric.getErrorCount());
            result.add(data);
        }

        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result);
    }
}