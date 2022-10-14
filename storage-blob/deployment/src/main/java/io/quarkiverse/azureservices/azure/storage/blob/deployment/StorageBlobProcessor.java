package io.quarkiverse.azureservices.azure.storage.blob.deployment;

import io.quarkiverse.azureservices.azure.storage.blob.runtime.StorageBlobServiceClientProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class StorageBlobProcessor {

    static final String FEATURE = "azure-storage-blob";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public AdditionalBeanBuildItem producer() {
        return new AdditionalBeanBuildItem(StorageBlobServiceClientProducer.class);
    }
}
