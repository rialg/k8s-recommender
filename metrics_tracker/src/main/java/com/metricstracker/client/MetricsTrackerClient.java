package com.metricstracker.client;

import com.metricstracker.proto.*;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;

public class MetricsTrackerClient {
    private final MetricsTrackerGrpc.MetricsTrackerBlockingStub blockingStub;

    public MetricsTrackerClient(Channel channel) {
        blockingStub = MetricsTrackerGrpc.newBlockingStub(channel);
    }

    public void getAllMetrics() {
        MetricsRequest request = MetricsRequest.newBuilder().build();
        
        try {
        
            System.out.println("Requesting Pod CPU Metrics...");
            PodMetricsResponse podCPUResponse = blockingStub.getPodsCPUUtilization(request);
            System.out.println("Pod CPU Metrics: " + podCPUResponse);
        
        } catch (StatusRuntimeException e) {
        
            System.out.println("RPC failed: " + e.getStatus());
        
        }
        try {
            System.out.println("\nRequesting Pod Memory Metrics...");
            PodMetricsResponse podMemResponse = blockingStub.getPodsMemoryUtilization(request);
            System.out.println("Pod Memory Metrics: " + podMemResponse);

        } catch (StatusRuntimeException e) {
        
            System.out.println("RPC failed: " + e.getStatus());
        
        }
        try {

            System.out.println("\nRequesting Pod Hits Metrics...");
            PodMetricsResponse podHitsResponse = blockingStub.getPodsHitsUtilization(request);
            System.out.println("Pod Hits Metrics: " + podHitsResponse);

        } catch (StatusRuntimeException e) {
        
            System.out.println("RPC failed: " + e.getStatus());
        
        }
        try {

            System.out.println("\nRequesting Node CPU Metrics...");
            NodeMetricsResponse nodeCPUResponse = blockingStub.getNodesCPUUtilization(request);
            System.out.println("Node CPU Metrics: " + nodeCPUResponse);

        } catch (StatusRuntimeException e) {
        
            System.out.println("RPC failed: " + e.getStatus());
        
        }
        try {

            System.out.println("\nRequesting Node Memory Metrics...");
            NodeMetricsResponse nodeMemResponse = blockingStub.getNodesMemoryUtilization(request);
            System.out.println("Node Memory Metrics: " + nodeMemResponse);

        } catch (StatusRuntimeException e) {
            System.out.println("RPC failed: " + e.getStatus());
        }
    }

    public static void main(String[] args) throws Exception {
        String target = "localhost:50051";
        
        // Create a channel
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
            .usePlaintext()  // For testing only, don't use in production without TLS
            .build();
        
        try {
            MetricsTrackerClient client = new MetricsTrackerClient(channel);
            client.getAllMetrics();
        } finally {
            // Shutdown the channel
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
