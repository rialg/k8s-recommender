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
package com.scalingcontroller;

import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.Quantity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

public class Service {
    private String name;
    private String statefulSetName;
    private String image;
    private ResourceRequirements resources;
    private int replicas;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatefulSetName() {
        return statefulSetName;
    }

    public void setStatefulSetName(String statefulSetName) {
        this.statefulSetName = statefulSetName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ResourceRequirements getResources() {
        return resources;
    }

    public void setResources(ResourceRequirements resources) {
        this.resources = resources;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Service service = (Service) o;
        if (replicas != service.replicas) return false;
        if (!name.equals(service.name)) return false;
        if (!statefulSetName.equals(service.statefulSetName)) return false;
        if (!image.equals(service.image)) return false;
        return resources.equals(service.resources);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + statefulSetName.hashCode();
        result = 31 * result + image.hashCode();
        result = 31 * result + resources.hashCode();
        result = 31 * result + replicas;
        return result;
    }

    @Override
    public String toString() {
        return "Service{" +
               "name='" + name + '\'' +
               ", statefulSetName='" + statefulSetName + '\'' +
               ", image='" + image + '\'' +
               ", resources=" + resources +
               ", replicas=" + replicas +
               '}';
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class ResourceRequirements {
    private Map<String, Quantity> requests;
    private Map<String, Quantity> limits;

    public Map<String, Quantity> getRequests() {
        return requests;
    }

    public void setRequests(Map<String, Quantity> requests) {
        this.requests = requests;
    }

    public Map<String, Quantity> getLimits() {
        return limits;
    }

    public void setLimits(Map<String, Quantity> limits) {
        this.limits = limits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ResourceRequirements that = (ResourceRequirements) o;
        if (requests != null ? !requests.equals(that.requests) : that.requests != null) return false;
        return limits != null ? limits.equals(that.limits) : that.limits == null;
    }

    @Override
    public int hashCode() {
        int result = requests != null ? requests.hashCode() : 0;
        result = 31 * result + (limits != null ? limits.hashCode() : 0);
        return result;
    }
}
