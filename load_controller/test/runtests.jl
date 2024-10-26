
#=
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
=#

using Test
using Statistics
using LoadController

@testset "LoadController Tests" begin
    @testset "Input Validation Tests" begin
        # Valid test data
        valid_cpu = [45.2, 48.7, 52.1, 49.8, 51.2]
        valid_memory = [62.3, 63.1, 64.5, 63.8, 65.2]
        
        # Test empty pod name
        @test_throws ArgumentError get_pod_next_util("", valid_cpu, valid_memory)
        @test_throws ArgumentError get_node_next_util("", valid_cpu, valid_memory)
        
        # Test empty usage vectors
        @test_throws ArgumentError get_pod_next_util("test-pod", Float64[], valid_memory)
        @test_throws ArgumentError get_pod_next_util("test-pod", valid_cpu, Float64[])
        
        # Test mismatched vector lengths
        @test_throws ArgumentError get_pod_next_util("test-pod", valid_cpu[1:4], valid_memory)
        @test_throws ArgumentError get_node_next_util("test-node", valid_cpu[1:4], valid_memory)
        
        # Test invalid usage values (outside 0-100 range)
        invalid_cpu = [45.2, 148.7, 52.1, 49.8, 51.2]
        invalid_memory = [62.3, 63.1, -64.5, 63.8, 65.2]
        @test_throws ArgumentError get_pod_next_util("test-pod", invalid_cpu, valid_memory)
        @test_throws ArgumentError get_pod_next_util("test-pod", valid_cpu, invalid_memory)
        @test_throws ArgumentError get_node_next_util("test-node", invalid_cpu, valid_memory)
        @test_throws ArgumentError get_node_next_util("test-node", valid_cpu, invalid_memory)
    end
    
    @testset "Pod Prediction Tests" begin
        # Test data with clear trend
        trending_cpu = [40.0, 45.0, 50.0, 55.0, 60.0]
        trending_memory = [60.0, 65.0, 70.0, 75.0, 80.0]
        
        # Get predictions
        pred_cpu, pred_mem = get_pod_next_util("test-pod", trending_cpu, trending_memory)
        
        # Test prediction shape
        @test length(pred_cpu) == 3  # Default prediction length
        @test length(pred_mem) == 3
        
        # Test predictions are within valid range
        @test all(0 .<= pred_cpu .<= 100)
        @test all(0 .<= pred_mem .<= 100)
        
        # Test predictions follow trend direction
        @test pred_cpu[1] < trending_cpu[end]
        @test pred_mem[1] < trending_memory[end]
    end
    
    @testset "Node Prediction Tests" begin
        # Test data with oscillating pattern
        oscillating_cpu = [70.0, 75.0, 73.0, 77.0, 74.0]
        oscillating_memory = [80.0, 85.0, 83.0, 87.0, 84.0]
        
        # Get predictions
        pred_cpu, pred_mem = get_node_next_util("test-node", oscillating_cpu, oscillating_memory)
        
        # Test prediction shape
        @test length(pred_cpu) == 3
        @test length(pred_mem) == 3
        
        # Test predictions are within valid range
        @test all(0 .<= pred_cpu .<= 100)
        @test all(0 .<= pred_mem .<= 100)
        
        # Test predictions are near the mean of historical values
        @test abs(mean(pred_cpu) - mean(oscillating_cpu)) < 20
        @test abs(mean(pred_mem) - mean(oscillating_memory)) < 20
    end
    
    @testset "Stability Tests" begin
        # Test with constant values
        constant_cpu = fill(50.0, 5)
        constant_memory = fill(70.0, 5)
        
        pred_cpu, pred_mem = get_pod_next_util("test-pod", constant_cpu, constant_memory)
        
        # Test that predictions for constant input stay relatively constant
        @test all(abs.(pred_cpu .- 50.0) .< 10.0)
        @test all(abs.(pred_mem .- 70.0) .< 10.0)
        
        # Test with small random fluctuations
        random_cpu = 50.0 .+ rand(-5:5, 5)
        random_memory = 70.0 .+ rand(-5:5, 5)
        
        pred_cpu, pred_mem = get_node_next_util("test-node", random_cpu, random_memory)
        
        # Test that predictions for random input stay within reasonable bounds
        @test all(abs.(pred_cpu .- 50.0) .< 15.0)
        @test all(abs.(pred_mem .- 70.0) .< 15.0)
    end
    
    @testset "Edge Case Tests" begin
        # Test with minimum valid values
        min_cpu = fill(0.0, 5)
        min_memory = fill(0.0, 5)
        pred_cpu, pred_mem = get_pod_next_util("test-pod", min_cpu, min_memory)
        @test all(pred_cpu .>= 0.0)
        @test all(pred_mem .>= 0.0)
        
        # Test with maximum valid values
        max_cpu = fill(100.0, 5)
        max_memory = fill(100.0, 5)
        pred_cpu, pred_mem = get_node_next_util("test-node", max_cpu, max_memory)
        @test all(pred_cpu .<= 100.0)
        @test all(pred_mem .<= 100.0)
        
        # Test with sudden spikes
        spike_cpu = [50.0, 50.0, 90.0, 50.0, 50.0]
        spike_memory = [70.0, 70.0, 95.0, 70.0, 70.0]
        pred_cpu, pred_mem = get_pod_next_util("test-pod", spike_cpu, spike_memory)
        @test all(0 .<= pred_cpu .<= 100)
        @test all(0 .<= pred_mem .<= 100)
    end
end
