package io.quarkiverse.azure.storage.blob.runtime;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.azure.core.util.ClientOptions;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

public class StorageBlobServiceClientProducer {

    private static final String AZURE_QUARKUS_STORAGE_BLOB = "az-qk-storage-blob";

    @Inject
    StorageBlobConfig storageBlobConfiguration;

    @Produces
    public BlobServiceClient blobServiceClient() {
        return new BlobServiceClientBuilder()
                .clientOptions(new ClientOptions().setApplicationId(AZURE_QUARKUS_STORAGE_BLOB))
                .connectionString(storageBlobConfiguration.connectionString)
                .buildClient();
    }
}
