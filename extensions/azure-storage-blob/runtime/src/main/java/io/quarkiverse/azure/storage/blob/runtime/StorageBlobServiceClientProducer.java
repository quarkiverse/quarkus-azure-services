package io.quarkiverse.azure.storage.blob.runtime;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.azure.core.util.ClientOptions;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import io.quarkiverse.azure.core.util.AzureQuarkusIdentifier;

public class StorageBlobServiceClientProducer {

    @Inject
    StorageBlobConfig storageBlobConfiguration;

    @Produces
    public BlobServiceClient blobServiceClient() {
        if (!storageBlobConfiguration.enabled) {
            return null;
        }

        assert storageBlobConfiguration.connectionString.isPresent()
                : "The connection string of Azure Storage Account must be set";
        return new BlobServiceClientBuilder()
                .clientOptions(new ClientOptions().setApplicationId(AzureQuarkusIdentifier.AZURE_QUARKUS_STORAGE_BLOB))
                .connectionString(storageBlobConfiguration.connectionString.get())
                .buildClient();
    }

    @Produces
    public BlobServiceAsyncClient blobServiceAsyncClient() {
        if (!storageBlobConfiguration.enabled) {
            return null;
        }

        assert storageBlobConfiguration.connectionString.isPresent()
                : "The connection string of Azure Storage Account must be set";
        return new BlobServiceClientBuilder()
                .clientOptions(new ClientOptions().setApplicationId(AzureQuarkusIdentifier.AZURE_QUARKUS_STORAGE_BLOB))
                .connectionString(storageBlobConfiguration.connectionString.get())
                .buildAsyncClient();
    }
}
