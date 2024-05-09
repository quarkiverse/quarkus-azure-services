package io.quarkiverse.azure.services.disabled.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;

@Path("/quarkus-azure-storage-blob-disabled")
@Produces(MediaType.TEXT_PLAIN)
@ApplicationScoped
public class StorageBlobDisabledResource {

    @Inject
    BlobServiceClient blobServiceClient;

    @Inject
    BlobServiceAsyncClient blobServiceAsyncClient;

    @Path("/blobServiceClient")
    @GET
    public String getBlobServiceClient() {
        assert blobServiceClient == null : "The BlobServiceClient should be null";
        return "The BlobServiceClient is null because the Azure Storage Blob is disabled";
    }

    @Path("/blobServiceAsyncClient")
    @GET
    public String getBlobServiceAsyncClient() {
        assert blobServiceAsyncClient == null : "The BlobServiceAsyncClient should be null";
        return "The BlobServiceAsyncClient is null because the Azure Storage Blob is disabled";
    }
}
