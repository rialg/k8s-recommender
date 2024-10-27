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

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import io.fabric8.kubernetes.model.annotation.ShortNames;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(
    using = JsonDeserializer.None.class
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Group("scalingcontroller.com")
@Version("v1")
@ShortNames("appprov")
public class ApplicationProvisioner extends CustomResource<ApplicationProvisionerSpec, Void> implements Namespaced {
    
    public ApplicationProvisioner() {
        super();
    }

    @Override
    protected ApplicationProvisionerSpec initSpec() {
        return new ApplicationProvisionerSpec();
    }

    @Override
    public String toString() {
        return "ApplicationProvisioner{" +
               "apiVersion='" + getApiVersion() + "'" +
               ", metadata=" + getMetadata() +
               ", spec=" + getSpec() +
               "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        
        ApplicationProvisioner that = (ApplicationProvisioner) o;
        return getSpec() != null ? getSpec().equals(that.getSpec()) : that.getSpec() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getSpec() != null ? getSpec().hashCode() : 0);
        return result;
    }
}
