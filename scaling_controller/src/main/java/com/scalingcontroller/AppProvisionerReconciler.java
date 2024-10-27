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

import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerConfiguration
public class AppProvisionerReconciler implements Reconciler<ApplicationProvisioner> {
    private static final Logger log = LoggerFactory.getLogger(AppProvisionerReconciler.class);

    @Override
    public UpdateControl<ApplicationProvisioner> reconcile(
            ApplicationProvisioner resource,
            Context<ApplicationProvisioner> context) {

        log.info("Reconciling ApplicationProvisioner: {}", resource.getMetadata().getName());

        for (Service service : resource.getSpec().getServices()) {
            StatefulSet statefulSet = createStatefulSet(service, resource);
            
            context.getClient().apps().statefulSets()
                .inNamespace(resource.getMetadata().getNamespace())
                .createOrReplace(statefulSet);
        }

        return UpdateControl.noUpdate();
    }

    private StatefulSet createStatefulSet(Service service, ApplicationProvisioner resource) {
        return new StatefulSetBuilder()
            .withNewMetadata()
                .withName(service.getStatefulSetName())
                .withNamespace(resource.getMetadata().getNamespace())
                .addToLabels("app", service.getName())
                .addToOwnerReferences(createOwnerReference(resource))
            .endMetadata()
            .withNewSpec()
                .withReplicas(service.getReplicas())
                .withNewSelector()
                    .addToMatchLabels("app", service.getName())
                .endSelector()
                .withNewTemplate()
                    .withNewMetadata()
                        .addToLabels("app", service.getName())
                    .endMetadata()
                    .withNewSpec()
                        .addNewContainer()
                            .withName(service.getName())
                            .withImage(service.getImage())
                            .withNewResources()
                                .addToRequests(service.getResources().getRequests())
                                .addToLimits(service.getResources().getLimits())
                            .endResources()
                        .endContainer()
                    .endSpec()
                .endTemplate()
            .endSpec()
            .build();
    }

    private OwnerReference createOwnerReference(ApplicationProvisioner resource) {
        return new OwnerReferenceBuilder()
            .withApiVersion(resource.getApiVersion())
            .withKind(resource.getKind())
            .withName(resource.getMetadata().getName())
            .withUid(resource.getMetadata().getUid())
            .withBlockOwnerDeletion(true)
            .withController(true)
            .build();
    }

    public static void main(String[] args) {
        io.javaoperatorsdk.operator.Operator operator = new io.javaoperatorsdk.operator.Operator();
        operator.register(new AppProvisionerReconciler());
        operator.start();
    }
}
