package io.quarkiverse.azure.services.disabled.it;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
    public Response getBlobServiceClient() {
        assert blobServiceClient == null : "The BlobServiceClient should be null";
        return Response.status(NOT_FOUND).entity("The BlobServiceClient is null because the Azure Storage Blob is disabled")
                .build();
    }

    @Path("/blobServiceAsyncClient")
    @GET
    public Response getBlobServiceAsyncClient() {
        assert blobServiceAsyncClient == null : "The BlobServiceAsyncClient should be null";
        return Response.status(NOT_FOUND)
                .entity("The BlobServiceAsyncClient is null because the Azure Storage Blob is disabled").build();
    }
}
