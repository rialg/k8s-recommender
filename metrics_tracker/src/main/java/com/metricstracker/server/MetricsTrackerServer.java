package com.metricstracker.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import com.metricstracker.service.PrometheusMetricsService;
import com.metricstracker.service.PrometheusClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsTrackerServer {
    private static final Logger logger = LoggerFactory.getLogger(MetricsTrackerServer.class);
    private static final int PORT = 50051;
    private Server server;

    // Prometheus configuration - these could be moved to a config file
    private static final String PROMETHEUS_URL = "http://localhost:9090"; // Default Prometheus URL

    private void start() throws Exception {
        // Initialize PrometheusClient
        PrometheusClient prometheusClient = PrometheusClient.builder()
            .baseUrl(PROMETHEUS_URL)
            .build();

        // Initialize PrometheusMetricsService using the builder pattern
        PrometheusMetricsService metricsService = PrometheusMetricsService.builder()
            .client(prometheusClient)
            .build();

        // Create and start the gRPC server
        server = ServerBuilder.forPort(PORT)
            .addService(new MetricsTrackerService(metricsService))
            .build()
            .start();
            
        logger.info("Server started, listening on port {}", PORT);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("Shutting down gRPC server due to JVM shutdown");
                MetricsTrackerServer.this.stop();
            }
        });
    }

    private void stop() {
        if (server != null) {
            try {
                server.shutdown().awaitTermination();
                logger.info("Server shut down successfully");
            } catch (InterruptedException e) {
                logger.error("Error during server shutdown", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) {
        MetricsTrackerServer server = new MetricsTrackerServer();
        try {
            server.start();
            server.blockUntilShutdown();
        } catch (Exception e) {
            logger.error("Server failed to start", e);
            System.exit(1);
        }
    }

    /**
     * Builder pattern for server configuration
     */
    public static class Builder {
        private String prometheusUrl = PROMETHEUS_URL;
        private int serverPort = PORT;

        public Builder prometheusUrl(String url) {
            this.prometheusUrl = url;
            return this;
        }

        public Builder serverPort(int port) {
            this.serverPort = port;
            return this;
        }

        public MetricsTrackerServer build() {
            return new MetricsTrackerServer();
        }
    }
}
