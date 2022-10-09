package io.quarkiverse.azureservices.azure.storage.blob.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class AzureStorageBlobProcessor {

    private static final String FEATURE = "azure-storage-blob";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
}
