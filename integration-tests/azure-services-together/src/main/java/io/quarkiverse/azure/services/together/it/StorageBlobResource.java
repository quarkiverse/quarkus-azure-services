package io.quarkiverse.azure.services.together.it;

import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;

@Path("/quarkus-services-azure-storage-blob")
@Produces(MediaType.TEXT_PLAIN)
@ApplicationScoped
public class StorageBlobResource {

    @Inject
    BlobServiceClient blobServiceClient;

    @Path("/{container}/{blobName}")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public Response uploadBlob(
            @PathParam("container") String container,
            @PathParam("blobName") String blobName,
            String body) {
        BlobContainerClient blobContainerClient = blobServiceClient.createBlobContainerIfNotExists(container);
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
        blobClient.upload(BinaryData.fromString(body), true);

        return Response.status(CREATED).build();
    }

    @Path("/{container}/{blobName}")
    @GET
    public Response downloadBlob(
            @PathParam("container") String container,
            @PathParam("blobName") String blobName) {
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(container);
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        if (blobClient.exists()) {
            return Response.ok().entity(blobClient.downloadContent().toString()).build();
        } else {
            return Response.status(NOT_FOUND).build();
        }

    }

    @Path("/{container}/{blobName}")
    @DELETE
    public Response delete(
            @PathParam("container") String container,
            @PathParam("blobName") String blobName) {
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(container);
        blobContainerClient.getBlobClient(blobName).delete();

        return Response.noContent().build();
    }

    @Path("/{container}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> listBlobs(@PathParam("container") String container) {
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(container);
        return blobContainerClient.listBlobs().stream()
                .map(BlobItem::getName)
                .collect(Collectors.toList());
    }

}
