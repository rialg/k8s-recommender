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
package com.metricstracker.client;

import com.metricstracker.model.PodMetric;
import com.metricstracker.model.NodeMetric;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrometheusClientTest {
    private MockWebServer mockWebServer;
    private PrometheusClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        client = new PrometheusClient(mockWebServer.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void queryPodMetrics_Success() {
        // Arrange
        String responseBody = """
            {
              "status": "success",
              "data": {
                "resultType": "vector",
                "result": [
                  {
                    "metric": {"pod": "test-pod-1"},
                    "value": [1641000000, "0.75"]
                  },
                  {
                    "metric": {"pod": "test-pod-2"},
                    "value": [1641000000, "0.85"]
                  }
                ]
              }
            }
            """;
        mockWebServer.enqueue(new MockResponse()
            .setBody(responseBody)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        // Act
        List<PodMetric> metrics = client.queryPodMetrics("test_query");

        // Assert
        assertEquals(2, metrics.size());
        assertEquals("test-pod-1", metrics.get(0).podName());
        assertEquals(0.75, metrics.get(0).value(), 0.001);
        assertEquals("1641000000", metrics.get(0).timestamp());
    }

    @Test
    void queryPodMetrics_EmptyResponse() {
        // Arrange
        String responseBody = """
            {
              "status": "success",
              "data": {
                "resultType": "vector",
                "result": []
              }
            }
            """;
        mockWebServer.enqueue(new MockResponse()
            .setBody(responseBody)
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json"));

        // Act
        List<PodMetric> metrics = client.queryPodMetrics("test_query");

        // Assert
        assertTrue(metrics.isEmpty());
    }

    @Test
    void queryPodMetrics_ErrorResponse() {
        // Arrange
        String responseBody = """
            {
              "status": "error",
              "errorType": "bad_data",
              "error": "invalid query"
            }
            """;
        mockWebServer.enqueue(new MockResponse()
            .setBody(responseBody)
            .setResponseCode(400)
            .addHeader("Content-Type", "application/json"));

        // Act & Assert
        assertThrows(PrometheusClient.PrometheusQueryException.class, 
            () -> client.queryPodMetrics("invalid_query"));
    }
}
