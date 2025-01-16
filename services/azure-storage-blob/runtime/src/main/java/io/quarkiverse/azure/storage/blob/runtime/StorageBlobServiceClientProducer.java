package io.quarkiverse.azure.storage.blob.runtime;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.azure.core.util.ClientOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import io.quarkiverse.azure.core.util.AzureQuarkusIdentifier;

public class StorageBlobServiceClientProducer {

    @Inject
    StorageBlobConfig storageBlobConfiguration;

    @Produces
    public BlobServiceClient blobServiceClient() {
        BlobServiceClientBuilder builder = getBuilder();
        return null == builder ? null : builder.buildClient();
    }

    @Produces
    public BlobServiceAsyncClient blobServiceAsyncClient() {
        BlobServiceClientBuilder builder = getBuilder();
        return null == builder ? null : builder.buildAsyncClient();
    }

    private BlobServiceClientBuilder getBuilder() {
        if (!storageBlobConfiguration.enabled()) {
            return null;
        }

        if (storageBlobConfiguration.endpoint().isEmpty() && storageBlobConfiguration.connectionString().isEmpty()) {
            throw new IllegalArgumentException("The endpoint or connection string of Azure Storage blob must be set");
        }

        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
                .clientOptions(new ClientOptions().setApplicationId(AzureQuarkusIdentifier.AZURE_QUARKUS_STORAGE_BLOB));
        if (storageBlobConfiguration.connectionString().isPresent()) {
            return builder.connectionString(storageBlobConfiguration.connectionString().get());
        } else {
            return builder.endpoint(storageBlobConfiguration.endpoint().get())
                    .credential(new DefaultAzureCredentialBuilder().build());
        }
    }
}
