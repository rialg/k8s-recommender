package com.scalingcontroller;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

import java.util.List;

public class ApplicationProvisionerSpec {
    private List<Service> services;

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ApplicationProvisionerSpec that = (ApplicationProvisionerSpec) o;
        return services != null ? services.equals(that.services) : that.services == null;
    }

    @Override
    public int hashCode() {
        return services != null ? services.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ApplicationProvisionerSpec{" +
               "services=" + services +
               '}';
    }
}
