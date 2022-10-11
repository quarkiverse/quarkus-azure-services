package io.quarkiverse.azureservices.azure.storage.blob.runtime;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

public class StorageBlobServiceClientProducer {

    @Inject
    StorageBlobConfiguration storageBlobConfiguration;

    @Produces
    @Singleton
    @Default
    public BlobServiceClient blobServiceClient() {
        return new BlobServiceClientBuilder().connectionString(storageBlobConfiguration.connectionString).buildClient();
    }
}
