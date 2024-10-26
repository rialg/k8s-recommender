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
package com.metricstracker.server;

import com.metricstracker.proto.*;
import com.metricstracker.service.MetricsService;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

public class MetricsTrackerServiceTest {
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private MetricsService metricsService;

    @Test
    public void getPodsCPUUtilization_ReturnsCorrectResponse() throws Exception {
        // Arrange
        String serverName = InProcessServerBuilder.generateName();
        var service = new MetricsTrackerServiceImpl(metricsService);
        
        grpcCleanup.register(InProcessServerBuilder
            .forName(serverName)
            .directExecutor()
            .addService(service)
            .build()
            .start());

        MetricsTrackerGrpc.MetricsTrackerBlockingStub blockingStub = MetricsTrackerGrpc.newBlockingStub(
            grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName)
                    .directExecutor()
                    .build()));

        var podMetrics = Arrays.asList(
            new PodMetric("pod1", 0.5, "2024-01-01T00:00:00Z"),
            new PodMetric("pod2", 0.7, "2024-01-01T00:00:00Z")
        );
        when(metricsService.getPodsCPUUtilization(30)).thenReturn(podMetrics);

        // Act
        var response = blockingStub.getPodsCPUUtilization(
            MetricsRequest.newBuilder().setSeconds(30).build());

        // Assert
        assertEquals(2, response.getMetricsCount());
        assertEquals("pod1", response.getMetrics(0).getPodName());
        assertEquals(0.5, response.getMetrics(0).getValue(), 0.001);
    }
}
