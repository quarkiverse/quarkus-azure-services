package io.quarkiverse.azure.storage.blob.runtime;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.azure.core.util.ClientOptions;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import io.quarkiverse.azure.core.util.AzureQuarkusIdentifier;

public class StorageBlobServiceClientProducer {

    @Inject
    StorageBlobConfig storageBlobConfiguration;

    @Produces
    public BlobServiceClient blobServiceClient() {
        return new BlobServiceClientBuilder()
                .clientOptions(new ClientOptions().setApplicationId(AzureQuarkusIdentifier.AZURE_QUARKUS_STORAGE_BLOB))
                .connectionString(storageBlobConfiguration.connectionString)
                .buildClient();
    }
}
