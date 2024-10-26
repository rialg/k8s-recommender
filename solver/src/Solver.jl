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

module Solver

using Evolutionary
using Statistics
using LoadController

"""
    PodConfig
    
Structure to hold pod configuration parameters
"""
struct PodConfig
    name::String
    min_replicas::Int
    max_replicas::Int
    target_cpu_utilization::Float64
    target_memory_utilization::Float64
    current_replicas::Int
end

"""
    ObjectiveResult

Structure to hold optimization objective results
"""
struct ObjectiveResult
    fitness::Float64
    predicted_cpu::Float64
    predicted_memory::Float64
    suggested_replicas::Int
end

"""
    calculate_objective(replicas::Int, pod_config::PodConfig, cpu_usage::Vector{Float64}, memory_usage::Vector{Float64})

Calculates the fitness value for a given number of replicas based on predicted resource utilization.
Returns ObjectiveResult containing fitness score and predictions.
"""
function calculate_objective(replicas::Int, pod_config::PodConfig, cpu_usage::Vector{Float64}, memory_usage::Vector{Float64})
    # Get predictions for next utilization values
    predicted_cpu, predicted_memory = get_pod_next_util(pod_config.name, cpu_usage, memory_usage)
    
    # Calculate average predictions
    avg_cpu_prediction = mean(predicted_cpu)
    avg_memory_prediction = mean(predicted_memory)
    
    # Calculate expected utilization with the proposed number of replicas
    expected_cpu = avg_cpu_prediction * pod_config.current_replicas / replicas
    expected_memory = avg_memory_prediction * pod_config.current_replicas / replicas
    
    # Calculate how far we are from target utilizations
    cpu_deviation = abs(expected_cpu - pod_config.target_cpu_utilization)
    memory_deviation = abs(expected_memory - pod_config.target_memory_utilization)
    
    # Penalty for being too far from target utilization
    utilization_penalty = cpu_deviation + memory_deviation
    
    # Penalty for number of replicas (to prefer smaller numbers when possible)
    replica_penalty = 0.1 * replicas
    
    # Calculate fitness (lower is better)
    fitness = utilization_penalty + replica_penalty
    
    return ObjectiveResult(fitness, expected_cpu, expected_memory, replicas)
end

"""
    optimize_replicas(pod_config::PodConfig, cpu_usage::Vector{Float64}, memory_usage::Vector{Float64})

Uses evolutionary algorithms to find the optimal number of replicas for a pod.
Returns a tuple of (optimal_replicas::Int, objective_result::ObjectiveResult)
"""
function optimize_replicas(pod_config::PodConfig, cpu_usage::Vector{Float64}, memory_usage::Vector{Float64})
    # Define the objective function for optimization
    function objective(x::Vector{Float64})
        replicas = round(Int, x[1])
        replicas = clamp(replicas, pod_config.min_replicas, pod_config.max_replicas)
        return calculate_objective(replicas, pod_config, cpu_usage, memory_usage).fitness
    end
    
    # Define optimization bounds
    lower = [float(pod_config.min_replicas)]
    upper = [float(pod_config.max_replicas)]
    
    # Configure the evolutionary algorithm
    opts = GA(
        populationSize = 50,
        selection = tournament(3),
        crossover = MILX(),
        mutation = gaussian(0.2),
    )
    
    # Run optimization
    result = Evolutionary.optimize(objective, BoxConstraints(lower, upper), opts)
    
    # Get the best solution
    optimal_replicas = round(Int, result.minimizer[1])
    optimal_replicas = clamp(optimal_replicas, pod_config.min_replicas, pod_config.max_replicas)
    
    # Calculate final objective results
    final_objective = calculate_objective(optimal_replicas, pod_config, cpu_usage, memory_usage)
    
    return (optimal_replicas, final_objective)
end

"""
    suggest_pod_replicas(
        pod_name::String,
        cpu_usage::Vector{Float64},
        memory_usage::Vector{Float64};
        min_replicas::Int=1,
        max_replicas::Int=10,
        target_cpu_utilization::Float64=70.0,
        target_memory_utilization::Float64=70.0,
        current_replicas::Int=1
    )

Main function to suggest optimal number of pod replicas based on usage history.
Returns a tuple of (suggested_replicas::Int, expected_cpu::Float64, expected_memory::Float64)
"""
function suggest_pod_replicas(
    pod_name::String,
    cpu_usage::Vector{Float64},
    memory_usage::Vector{Float64};
    min_replicas::Int=1,
    max_replicas::Int=10,
    target_cpu_utilization::Float64=70.0,
    target_memory_utilization::Float64=70.0,
    current_replicas::Int=1
)
    # Input validation
    if min_replicas < 1
        throw(ArgumentError("min_replicas must be at least 1"))
    end
    if max_replicas < min_replicas
        throw(ArgumentError("max_replicas must be greater than or equal to min_replicas"))
    end
    if target_cpu_utilization <= 0 || target_cpu_utilization >= 100
        throw(ArgumentError("target_cpu_utilization must be between 0 and 100"))
    end
    if target_memory_utilization <= 0 || target_memory_utilization >= 100
        throw(ArgumentError("target_memory_utilization must be between 0 and 100"))
    end
    
    # Create pod configuration
    pod_config = PodConfig(
        pod_name,
        min_replicas,
        max_replicas,
        target_cpu_utilization,
        target_memory_utilization,
        current_replicas
    )
    
    # Run optimization
    optimal_replicas, result = optimize_replicas(pod_config, cpu_usage, memory_usage)
    
    return (optimal_replicas, result.predicted_cpu, result.predicted_memory)
end

export PodConfig, ObjectiveResult, suggest_pod_replicas

end

# TODO: Extend with Node allocation and other optimization strategies
