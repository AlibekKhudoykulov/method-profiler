package com.profiler.agent.exporter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Mini HTTP server — exposes metrics over HTTP.
 * Default port: 9999
 *
 * Endpoints:
 * - GET /metrics  → JSON metrics
 * - GET /reset    → reset metrics
 */
public class HttpServer {
    private com.sun.net.httpserver.HttpServer server;

    public void start(int port) throws IOException {
        server = com.sun.net.httpserver.HttpServer.create(
                new InetSocketAddress(port), 0);

        server.createContext("/metrics", new MetricsHandler());
        server.createContext("/reset", new ResetHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("[Profiler] HTTP server started on port " + port);
        System.out.println("[Profiler] Visit: http://localhost:" + port + "/metrics");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    static class MetricsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String json = JsonExporter.exportAll();
                byte[] response = json.getBytes();

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, response.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            } catch (Exception e) {
                String error = "{\"error\":\"" + e.getMessage() + "\"}";
                exchange.sendResponseHeaders(500, error.length());
                exchange.getResponseBody().write(error.getBytes());
                exchange.getResponseBody().close();
            }
        }
    }

    static class ResetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            com.profiler.agent.collector.MetricsCollector.getInstance().reset();
            String response = "{\"status\":\"reset\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
    }
}