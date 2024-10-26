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

import com.metricstracker.service.PrometheusClient;
import com.metricstracker.model.PodMetric;
import com.metricstracker.model.NodeMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.util.Config;

import java.util.List;
import java.io.IOException;

public class PrometheusMetricsService implements MetricsService {
    private static final Logger logger = LoggerFactory.getLogger(PrometheusMetricsService.class);
    private final PrometheusClient client;

    // Query templates for different metrics
    private static final String POD_CPU_QUERY_TEMPLATE = 
        "sum(rate(container_cpu_usage_seconds_total{container!=\"\"}[%ds])) by (pod)";
    
    private static final String POD_MEMORY_QUERY_TEMPLATE = 
        "sum(container_memory_working_set_bytes{container!=\"\"}) by (pod)";
    
    private static final String POD_HITS_QUERY_TEMPLATE = 
        "sum(rate(http_server_requests_seconds_count{container!=\"\"}[%ds])) by (pod)";
    
    private static final String NODE_CPU_QUERY_TEMPLATE = 
        "sum(rate(node_cpu_seconds_total{mode!=\"idle\"}[%ds])) by (instance)";
    
    private static final String NODE_MEMORY_QUERY_TEMPLATE = 
        "sum(node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) by (instance) / sum(node_memory_MemTotal_bytes) by (instance) * 100";

    public PrometheusMetricsService(PrometheusClient client) {
        this.client = client;
    }

    @Override
    public List<PodMetric> getPodsCPUUtilization(int seconds) {
        logger.debug("Retrieving CPU utilization for pods over {} seconds", seconds);
        try {
            String query = String.format(POD_CPU_QUERY_TEMPLATE, seconds);
            List<PodMetric> metrics = client.queryPodMetrics(query);
            
            // Convert raw CPU values to percentage (multiply by 100)
            return metrics.stream()
                .map(metric -> new PodMetric(
                    metric.podName(),
                    metric.value() * 100, // Convert to percentage
                    metric.timestamp()
                ))
                .toList();
        } catch (Exception e) {
            logger.error("Failed to retrieve pod CPU metrics", e);
            throw new MetricsServiceException("Error retrieving pod CPU metrics", e);
        }
    }

    @Override
    public List<PodMetric> getPodsMemoryUtilization(int seconds) {
        logger.debug("Retrieving memory utilization for pods");
        try {
            // Memory query doesn't need time window as it's an instant value
            String query = POD_MEMORY_QUERY_TEMPLATE;
            List<PodMetric> metrics = client.queryPodMetrics(query);
            
            // Convert bytes to megabytes
            return metrics.stream()
                .map(metric -> new PodMetric(
                    metric.podName(),
                    metric.value() / (1024 * 1024), // Convert bytes to MB
                    metric.timestamp()
                ))
                .toList();
        } catch (Exception e) {
            logger.error("Failed to retrieve pod memory metrics", e);
            throw new MetricsServiceException("Error retrieving pod memory metrics", e);
        }
    }

    @Override
    public List<PodMetric> getPodsHitsUtilization(int seconds) {
        logger.debug("Retrieving HTTP hits for pods over {} seconds", seconds);
        try {
            String query = String.format(POD_HITS_QUERY_TEMPLATE, seconds);
            return client.queryPodMetrics(query);
        } catch (Exception e) {
            logger.error("Failed to retrieve pod hits metrics", e);
            throw new MetricsServiceException("Error retrieving pod hits metrics", e);
        }
    }

    @Override
    public List<NodeMetric> getNodesCPUUtilization(int seconds) {
        logger.debug("Retrieving CPU utilization for nodes over {} seconds", seconds);
        try {
            String query = String.format(NODE_CPU_QUERY_TEMPLATE, seconds);
            List<NodeMetric> metrics = client.queryNodeMetrics(query);
            
            // Convert raw CPU values to percentage (multiply by 100)
            return metrics.stream()
                .map(metric -> new NodeMetric(
                    getNodeNameByIP(metric.nodeName()
                                          .substring(0, metric.nodeName().indexOf(":"))),
                    metric.value() * 100, // Convert to percentage
                    metric.timestamp()
                ))
                .toList();
        } catch (Exception e) {
            logger.error("Failed to retrieve node CPU metrics", e);
            throw new MetricsServiceException("Error retrieving node CPU metrics", e);
        }
    }

    @Override
    public List<NodeMetric> getNodesMemoryUtilization(int seconds) {
        logger.debug("Retrieving memory utilization for nodes");
        try {
            // Memory percentage is calculated directly in the query
            String query = NODE_MEMORY_QUERY_TEMPLATE;
            List<NodeMetric> metrics = client.queryNodeMetrics(query);
            
            return metrics.stream()
                .map(metric -> new NodeMetric(
                    getNodeNameByIP(metric.nodeName()
                                          .substring(0, metric.nodeName().indexOf(":"))),
                    metric.value(), // Already in percentage
                    metric.timestamp()
                ))
                .toList();
        } catch (Exception e) {
            logger.error("Failed to retrieve node memory metrics", e);
            throw new MetricsServiceException("Error retrieving node memory metrics", e);
        }
    }

    /**
     * Get node name from IP address.
     */
    public static String getNodeNameByIP(String nodeIP) {
        // Load the default Kubernetes configuration from cluster or kubeconfig
        try {
            ApiClient client = Config.defaultClient();
            Configuration.setDefaultApiClient(client);
        
            // Create CoreV1Api client
            CoreV1Api api = new CoreV1Api();
            
            // Get list of all nodes
            V1NodeList nodeList = api.listNode(null, null, null, null, null, null, null, null, null, null);
            
            // Search through nodes to find matching IP
            return nodeList.getItems().stream()
                .filter(node -> node.getStatus().getAddresses().stream()
                    .anyMatch(address -> "InternalIP".equals(address.getType()) 
                        && nodeIP.equals(address.getAddress())))
                .findFirst()
                .map(node -> node.getMetadata().getName())
                .orElseThrow(() -> new RuntimeException("Node not found with IP: " + nodeIP));

        } catch (IOException | ApiException e) {
            throw new RuntimeException("Failed to load Kubernetes configuration", e);
        }
    }

    /**
     * Custom exception for metrics service errors
     */
    public static class MetricsServiceException extends RuntimeException {
        public MetricsServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Builder pattern for service configuration
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PrometheusClient client;

        public Builder client(PrometheusClient client) {
            this.client = client;
            return this;
        }

        public PrometheusMetricsService build() {
            if (client == null) {
                throw new IllegalStateException("PrometheusClient is required");
            }
            return new PrometheusMetricsService(client);
        }
    }
}
