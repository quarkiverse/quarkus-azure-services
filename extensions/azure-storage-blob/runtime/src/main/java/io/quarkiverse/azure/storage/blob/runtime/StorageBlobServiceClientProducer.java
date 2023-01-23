package io.quarkiverse.azure.storage.blob.runtime;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

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
