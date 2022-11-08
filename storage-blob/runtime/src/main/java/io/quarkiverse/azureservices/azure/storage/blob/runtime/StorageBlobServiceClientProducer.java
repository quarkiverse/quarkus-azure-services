package io.quarkiverse.azureservices.azure.storage.blob.runtime;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

public class StorageBlobServiceClientProducer {

    @Inject
    StorageBlobConfig storageBlobConfiguration;

    @Produces
    @ApplicationScoped
    public BlobServiceClient blobServiceClient() {
        return new BlobServiceClientBuilder().connectionString(storageBlobConfiguration.connectionString).buildClient();
    }
}
