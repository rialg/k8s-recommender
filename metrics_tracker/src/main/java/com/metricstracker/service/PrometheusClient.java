/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/
package com.metricstracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metricstracker.model.NodeMetric;
import com.metricstracker.model.PodMetric;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrometheusClient {
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PrometheusClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public List<PodMetric> queryPodMetrics(String query) {
        try {
            String response = executeQuery(query);
            return parsePodMetricsResponse(response);
        } catch (Exception e) {
            throw new PrometheusQueryException("Failed to query pod metrics", e);
        }
    }

    public List<NodeMetric> queryNodeMetrics(String query) {
        try {
            String response = executeQuery(query);
            return parseNodeMetricsResponse(response);
        } catch (Exception e) {
            throw new PrometheusQueryException("Failed to query node metrics", e);
        }
    }

    private String executeQuery(String query) throws IOException, InterruptedException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = baseUrl + "/api/v1/query?query=" + encodedQuery;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()  // Changed to GET as it's more standard for Prometheus API
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new PrometheusQueryException("Prometheus query failed with status: " + response.statusCode() + 
                                             ", body: " + response.body());
        }

        return response.body();
    }

    private List<PodMetric> parsePodMetricsResponse(String response) throws IOException {
        JsonNode root = objectMapper.readTree(response);
        
        if (!"success".equals(root.path("status").asText())) {
            throw new PrometheusQueryException("Query returned error status: " + root.path("error").asText());
        }

        JsonNode result = root.path("data").path("result");
        if (result.isEmpty()) {
            return Collections.emptyList();
        }

        List<PodMetric> metrics = new ArrayList<>();
        for (JsonNode item : result) {
            String podName = item.path("metric").path("pod").asText();
            JsonNode value = item.path("value");
            
            if (value.size() >= 2) {
                String timestamp = value.get(0).asText();
                double metricValue = value.get(1).asDouble();
                metrics.add(new PodMetric(podName, metricValue, timestamp));
            }
        }

        return metrics;
    }

    private List<NodeMetric> parseNodeMetricsResponse(String response) throws IOException {
        JsonNode root = objectMapper.readTree(response);
        
        if (!"success".equals(root.path("status").asText())) {
            throw new PrometheusQueryException("Query returned error status: " + root.path("error").asText());
        }

        JsonNode result = root.path("data").path("result");
        if (result.isEmpty()) {
            return Collections.emptyList();
        }

        List<NodeMetric> metrics = new ArrayList<>();
        for (JsonNode item : result) {
            String nodeName = item.path("metric").path("instance").asText();
            JsonNode value = item.path("value");
            
            if (value.size() >= 2) {
                String timestamp = value.get(0).asText();
                double metricValue = value.get(1).asDouble();
                metrics.add(new NodeMetric(nodeName, metricValue, timestamp));
            }
        }

        return metrics;
    }

    // Custom exception for Prometheus-related errors
    public static class PrometheusQueryException extends RuntimeException {
        public PrometheusQueryException(String message) {
            super(message);
        }

        public PrometheusQueryException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Builder pattern for client configuration
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String baseUrl = "http://localhost:9090";

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public PrometheusClient build() {
            return new PrometheusClient(baseUrl);
        }
    }
}
