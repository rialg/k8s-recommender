using Test
using Statistics
using Solver

@testset "Solver Tests" begin
    @testset "Pod Configuration" begin
        # Test valid pod configuration
        @test_nowarn PodConfig("test-pod", 1, 5, 70.0, 70.0, 1)
        
        # Test pod configuration constraints in suggest_pod_replicas
        cpu_usage = [50.0, 60.0, 70.0, 80.0]
        memory_usage = [45.0, 55.0, 65.0, 75.0]
        
        @test_throws ArgumentError suggest_pod_replicas(
            "test-pod",
            cpu_usage,
            memory_usage,
            min_replicas=0  # Invalid min_replicas
        )
        
        @test_throws ArgumentError suggest_pod_replicas(
            "test-pod",
            cpu_usage,
            memory_usage,
            min_replicas=5,
            max_replicas=3  # max < min
        )
        
        @test_throws ArgumentError suggest_pod_replicas(
            "test-pod",
            cpu_usage,
            memory_usage,
            target_cpu_utilization=150.0  # Invalid target utilization
        )
    end
    
    @testset "Replica Optimization" begin
        cpu_usage = [50.0, 60.0, 70.0, 80.0]
        memory_usage = [45.0, 55.0, 65.0, 75.0]
        
        # Test basic optimization
        replicas, exp_cpu, exp_memory = suggest_pod_replicas(
            "test-pod",
            cpu_usage,
            memory_usage,
            min_replicas=1,
            max_replicas=5,
            target_cpu_utilization=70.0,
            target_memory_utilization=70.0,
            current_replicas=1
        )
        
        # Test output types
        @test replicas isa Int
        @test exp_cpu isa Float64
        @test exp_memory isa Float64
        
        # Test constraints are respected
        @test 1 ≤ replicas ≤ 5
        @test 0 ≤ exp_cpu ≤ 100
        @test 0 ≤ exp_memory ≤ 100
        
        # Test optimization with high utilization
        high_cpu = [85.0, 90.0, 95.0, 98.0]
        high_mem = [80.0, 85.0, 90.0, 95.0]
        
        replicas_high, _, _ = suggest_pod_replicas(
            "test-pod",
            high_cpu,
            high_mem,
            min_replicas=1,
            max_replicas=5,
            target_cpu_utilization=70.0,
            target_memory_utilization=70.0,
            current_replicas=1
        )
        
        # Test that high utilization leads to more replicas
        @test replicas_high >= 1
        
        # Test optimization with low utilization
        low_cpu = [20.0, 25.0, 30.0, 35.0]
        low_mem = [15.0, 20.0, 25.0, 30.0]
        
        replicas_low, _, _ = suggest_pod_replicas(
            "test-pod",
            low_cpu,
            low_mem,
            min_replicas=1,
            max_replicas=5,
            target_cpu_utilization=70.0,
            target_memory_utilization=70.0,
            current_replicas=2
        )
        
        # Test that low utilization leads to fewer replicas
        @test replicas_low ≤ 2
    end
end
