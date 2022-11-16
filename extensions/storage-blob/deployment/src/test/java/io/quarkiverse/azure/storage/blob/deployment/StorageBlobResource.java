package io.quarkiverse.azure.storage.blob.deployment;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;

@Path("/storageblob")
public class StorageBlobResource {

    @Inject
    BlobServiceClient blobServiceClient;

    @GET
    public String test() {
        BlobContainerClient blobContainerClient = blobServiceClient.createBlobContainerIfNotExists("mycontainer");
        BlobClient blobClient = blobContainerClient.getBlobClient("myblob");
        blobClient.upload(BinaryData.fromString("samples"), true);

        return blobClient.downloadContent().toString();
    }
}
