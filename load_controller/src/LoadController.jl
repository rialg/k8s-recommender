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

module LoadController

using StatsBase
using Statistics
using LinearAlgebra
using Distributions
using Dates
using StateSpaceModels: LinearRegression, forecast, forecast_expected_value, fit!, StateSpaceModels

"""
    fit_linear_regression(data::Vector{Float64})

Fits a LinearRegression model to the input data and returns the fitted model.
"""
function fit_linear_regression(data::Vector{Float64})
    # Create and fit LinearRegression model
    n = length(data)
    X = hcat(ones(n), collect(1:n))
    y = data

    model = LinearRegression(X, y)
    fit!(model; save_hyperparameter_distribution=false)
    
    return model
end

"""
    predict_next_values(model::StateSpaceModels, data::Vector{Float64}, n_ahead::Int=3)

Predicts the next n_ahead values using the fitted state space model.
"""
function predict_next_values(model, data::Vector{Float64}, n_ahead::Int=3)
    # Make predictions
    n = length(data)
    X_forecast = hcat(ones(n_ahead), collect(n+1:n+n_ahead))
    forec = forecast(model, X_forecast)
    predictions = forecast_expected_value(forec)
    
    # Ensure we return exactly n_ahead predictions
    if length(predictions) > n_ahead
        predictions = predictions[1:n_ahead]
    elseif length(predictions) < n_ahead
        # If we got fewer predictions than requested, pad with last value
        last_pred = predictions[end]
        predictions = vcat(predictions, fill(last_pred, n_ahead - length(predictions)))
    end
    
    return predictions
end

"""
    validate_input(cpu_usage::Vector{Float64}, memory_usage::Vector{Float64})

Validates the input vectors for the prediction functions.
"""
function validate_input(cpu_usage::Vector{Float64}, memory_usage::Vector{Float64})
    if isempty(cpu_usage) || isempty(memory_usage)
        throw(ArgumentError("Usage vectors cannot be empty"))
    end
    
    if length(cpu_usage) != length(memory_usage)
        throw(ArgumentError("CPU and memory usage vectors must have the same length"))
    end
    
    if any(x -> x < 0 || x > 100, cpu_usage) || any(x -> x < 0 || x > 100, memory_usage)
        throw(ArgumentError("Usage values must be between 0 and 100"))
    end
    
    if length(cpu_usage) < 4
        throw(ArgumentError("Need at least 4 data points for prediction"))
    end
end

"""
    get_pod_next_util(pod_name::String, cpu_usage::Vector{Float64}, memory_usage::Vector{Float64})

Predicts the next CPU and memory utilization values for a given pod.
Returns a tuple of vectors (predicted_cpu, predicted_memory).
"""
function get_pod_next_util(pod_name::String, cpu_usage::Vector{Float64}, memory_usage::Vector{Float64})
    # Input validation
    if isempty(pod_name)
        throw(ArgumentError("Pod name cannot be empty"))
    end
    validate_input(cpu_usage, memory_usage)
    
    try
        # Fit models for both CPU and memory
        cpu_model = fit_linear_regression(cpu_usage)
        memory_model = fit_linear_regression(memory_usage)
        
        # Make predictions
        predicted_cpu = predict_next_values(cpu_model, cpu_usage)
        predicted_memory = predict_next_values(memory_model, memory_usage)
        
        # Ensure predictions are within valid range (0-100%)
        predicted_cpu = clamp.(predicted_cpu, 0.0, 100.0)
        predicted_memory = clamp.(predicted_memory, 0.0, 100.0)
        
        return (predicted_cpu, predicted_memory)
    catch e
        rethrow(e)
    end
end

"""
    get_node_next_util(node_name::String, cpu_usage::Vector{Float64}, memory_usage::Vector{Float64})

Predicts the next CPU and memory utilization values for a given node.
Returns a tuple of vectors (predicted_cpu, predicted_memory).
"""
function get_node_next_util(node_name::String, cpu_usage::Vector{Float64}, memory_usage::Vector{Float64})
    # Input validation
    if isempty(node_name)
        throw(ArgumentError("Node name cannot be empty"))
    end
    validate_input(cpu_usage, memory_usage)
    
    try
        # For nodes, we use the same SARIMA model but could extend this
        # to use different parameters or preprocessing if needed
        cpu_model = fit_linear_regression(cpu_usage)
        memory_model = fit_linear_regression(memory_usage)
        
        # Make predictions
        predicted_cpu = predict_next_values(cpu_model, cpu_usage)
        predicted_memory = predict_next_values(memory_model, memory_usage)
        
        # Ensure predictions are within valid range (0-100%)
        predicted_cpu = clamp.(predicted_cpu, 0.0, 100.0)
        predicted_memory = clamp.(predicted_memory, 0.0, 100.0)
        
        return (predicted_cpu, predicted_memory)
    catch e
        rethrow(e)
    end
end

export get_pod_next_util, get_node_next_util

end
