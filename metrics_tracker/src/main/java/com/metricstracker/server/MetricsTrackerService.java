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
import com.metricstracker.model.NodeMetric;
import com.metricstracker.model.PodMetric;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.stream.Collectors;

public class MetricsTrackerService extends MetricsTrackerGrpc.MetricsTrackerImplBase {
    private final MetricsService metricsService;
    private final int MEASSUREMENT_INTERVAL = 60;

    public MetricsTrackerService(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    private PodMetricsResponse buildPodsResponse(List<PodMetric> metrics) {
        return PodMetricsResponse.newBuilder()
            .addAllMetrics(metrics.stream()
                .map(PodMetric::toProto)
                .collect(Collectors.toList()))
            .build();
    }

    private NodeMetricsResponse buildNodesResponse(List<NodeMetric> metrics) {
        return NodeMetricsResponse.newBuilder()
            .addAllMetrics(metrics.stream()
                .map(NodeMetric::toProto)
                .collect(Collectors.toList()))
            .build();
    }

    @Override
    public void getPodsCPUUtilization(MetricsRequest request,
            StreamObserver<PodMetricsResponse> responseObserver) {
        responseObserver.onNext(buildPodsResponse(metricsService.getPodsCPUUtilization(this.MEASSUREMENT_INTERVAL)));
        responseObserver.onCompleted();
    }

    @Override
    public void getPodsMemoryUtilization(MetricsRequest request,
            StreamObserver<PodMetricsResponse> responseObserver) {
        responseObserver.onNext(buildPodsResponse(metricsService.getPodsMemoryUtilization(this.MEASSUREMENT_INTERVAL)));
        responseObserver.onCompleted();
    }

    @Override
    public void getPodsHitsUtilization(MetricsRequest request,
            StreamObserver<PodMetricsResponse> responseObserver) {
        responseObserver.onNext(buildPodsResponse(metricsService.getPodsHitsUtilization(this.MEASSUREMENT_INTERVAL)));
        responseObserver.onCompleted();
    }

    @Override
    public void getNodesCPUUtilization(MetricsRequest request,
            StreamObserver<NodeMetricsResponse> responseObserver) {
        responseObserver.onNext(buildNodesResponse(metricsService.getNodesCPUUtilization(this.MEASSUREMENT_INTERVAL)));
        responseObserver.onCompleted();
    }

    @Override
    public void getNodesMemoryUtilization(MetricsRequest request,
            StreamObserver<NodeMetricsResponse> responseObserver) {
        responseObserver.onNext(buildNodesResponse(metricsService.getNodesMemoryUtilization(this.MEASSUREMENT_INTERVAL)));
        responseObserver.onCompleted();
    }
}
