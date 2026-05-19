package com.profiler.dashboard.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MetricsService {

    private final RestTemplate restTemplate;

    @Value("${profiler.agent.url:http://localhost:9999}")
    private String agentUrl;

    public MetricsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getMetrics() {
        try {
            return restTemplate.getForObject(agentUrl + "/metrics", String.class);
        } catch (Exception e) {
            return "[]";
        }
    }

    public String reset() {
        try {
            return restTemplate.getForObject(agentUrl + "/reset", String.class);
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}