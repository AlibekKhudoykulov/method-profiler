package com.profiler.dashboard.controller;

import com.profiler.dashboard.service.MetricsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class MetricsController {

    private final MetricsService service;

    public MetricsController(MetricsService service) {
        this.service = service;
    }

    @GetMapping(value = "/metrics", produces = "application/json")
    public String getMetrics() {
        return service.getMetrics();
    }

    @PostMapping("/reset")
    public String reset() {
        return service.reset();
    }
}