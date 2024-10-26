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
package com.metricstracker.model;

import com.metricstracker.proto.PodMetric.Builder;

/**
 * Represents a metric measurement for a pod at a specific point in time.
 * 
 * @param podName   The name of the pod
 * @param value     The measured metric value
 * @param timestamp The ISO-8601 formatted timestamp of the measurement
 */
public record PodMetric(String podName, double value, String timestamp) {
    // Compact constructor for validation
    public PodMetric {
        if (podName == null || podName.isBlank()) {
            throw new IllegalArgumentException("Pod name cannot be null or blank");
        }
        if (timestamp == null || timestamp.isBlank()) {
            throw new IllegalArgumentException("Timestamp cannot be null or blank");
        }
    }

    /**
     * Converts this domain model PodMetric to its proto equivalent.
     *
     * @return A proto PodMetric object with the same values as this domain object
     */
    public com.metricstracker.proto.PodMetric toProto() {
        return com.metricstracker.proto.PodMetric.newBuilder()
            .setPodName(podName)
            .setValue(value)
            .setTimestamp(timestamp)
            .build();
    }
}
