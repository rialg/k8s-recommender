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

import com.metricstracker.proto.NodeMetric.Builder;

/**
 * Represents a metric measurement for a node at a specific point in time.
 * 
 * @param nodeName  The name of the node
 * @param value     The measured metric value
 * @param timestamp The ISO-8601 formatted timestamp of the measurement
 */
public record NodeMetric(String nodeName, double value, String timestamp) {
    // Compact constructor for validation
    public NodeMetric {
        if (nodeName == null || nodeName.isBlank()) {
            throw new IllegalArgumentException("Node name cannot be null or blank");
        }
        if (timestamp == null || timestamp.isBlank()) {
            throw new IllegalArgumentException("Timestamp cannot be null or blank");
        }
    }

    /**
     * Converts this domain model NodeMetric to its proto equivalent.
     *
     * @return A proto NodeMetric object with the same values as this domain object
     */
    public com.metricstracker.proto.NodeMetric toProto() {
        return com.metricstracker.proto.NodeMetric.newBuilder()
            .setNodeName(nodeName)
            .setValue(value)
            .setTimestamp(timestamp)
            .build();
    }
}
