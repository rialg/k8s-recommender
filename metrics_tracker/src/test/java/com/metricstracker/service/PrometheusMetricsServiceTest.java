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

import com.metricstracker.client.PrometheusClient;
import com.metricstracker.model.PodMetric;
import com.metricstracker.model.NodeMetric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrometheusMetricsServiceTest {
    
    @Mock
    private PrometheusClient client;
    
    private PrometheusMetricsService service;

    @BeforeEach
    void setUp() {
        service = new PrometheusMetricsService(client);
    }

    @Test
    void getPodsCPUUtilization_Success() {
        // Arrange
        List<PodMetric> rawMetrics = List.of(
            new PodMetric("pod1", 0.25, "1234567890"), // 25% CPU
            new PodMetric("pod2", 0.75, "1234567890")  // 75% CPU
        );
        when(client.queryPodMetrics(anyString())).thenReturn(rawMetrics);

        // Act
        List<PodMetric> metrics = service.getPodsCPUUtilization(30);

        // Assert
        assertEquals(2, metrics.size());
        assertEquals(25.0, metrics.get(0).value(), 0.001); // Converted to percentage
        assertEquals(75.0, metrics.get(1).value(), 0.001);
    }

    @Test
    void getPodsMemoryUtilization_Success() {
        // Arrange
        List<PodMetric> rawMetrics = List.of(
            new PodMetric("pod1", 104857600, "1234567890"),  // 100MB in bytes
            new PodMetric("pod2", 209715200, "1234567890")   // 200MB in bytes
        );
        when(client.queryPodMetrics(anyString())).thenReturn(rawMetrics);

        // Act
        List<PodMetric> metrics = service.getPodsMemoryUtilization(30);

        // Assert
        assertEquals(2, metrics.size());
        assertEquals(100.0, metrics.get(0).value(), 0.001); // Converted to MB
        assertEquals(200.0, metrics.get(1).value(), 0.001);
    }

    @Test
    void getNodesCPUUtilization_Success() {
        // Arrange
        List<NodeMetric> rawMetrics = List.of(
            new NodeMetric("ip-10-0-1-23.ec2.internal:9100", 0.45, "1234567890"),
            new NodeMetric("ip-10-0-1-24.ec2.internal:9100", 0.85, "1234567890")
        );
        when(client.queryNodeMetrics(anyString())).thenReturn(rawMetrics);

        // Act
        List<NodeMetric> metrics = service.getNodesCPUUtilization(30);

        // Assert
        assertEquals(2, metrics.size());
        assertEquals("ip-10-0-1-23", metrics.get(0).nodeName()); // Cleaned node name
        assertEquals(45.0, metrics.get(0).value(), 0.001);      // Converted to percentage
        assertEquals("ip-10-0-1-24", metrics.get(1).nodeName());
        assertEquals(85.0, metrics.get(1).value(), 0.001);
    }

    @Test
    void getNodesMemoryUtilization_Success() {
        // Arrange
        List<NodeMetric> rawMetrics = List.of(
            new NodeMetric("ip-10-0-1-23.ec2.internal:9100", 65.5, "1234567890"),
            new NodeMetric("ip-10-0-1-24.ec2.internal:9100", 78.3, "1234567890")
        );
        when(client.queryNodeMetrics(anyString())).thenReturn(rawMetrics);

        // Act
        List<NodeMetric> metrics = service.getNodesMemoryUtilization(30);

        // Assert
        assertEquals(2, metrics.size());
        assertEquals("ip-10-0-1-23", metrics.get(0).nodeName());
        assertEquals(65.5, metrics.get(0).value(), 0.001);
        assertEquals("ip-10-0-1-24", metrics.get(1).nodeName());
        assertEquals(78.3, metrics.get(1).value(), 0.001);
    }

    @Test
    void clientError_ThrowsServiceException() {
        // Arrange
        when(client.queryPodMetrics(anyString()))
            .thenThrow(new RuntimeException("Prometheus error"));

        // Act & Assert
        assertThrows(PrometheusMetricsService.MetricsServiceException.class,
            () -> service.getPodsCPUUtilization(30));
    }
}
