package io.quarkiverse.azure.storage.blob.runtime;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

public class StorageBlobServiceClientProducer {

    @Inject
    StorageBlobConfig storageBlobConfiguration;

    @Produces
    public BlobServiceClient blobServiceClient() {
        return new BlobServiceClientBuilder().connectionString(storageBlobConfiguration.connectionString).buildClient();
    }
}
