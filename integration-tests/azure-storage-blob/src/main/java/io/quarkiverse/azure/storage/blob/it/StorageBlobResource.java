/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkiverse.azure.storage.blob.it;

import static jakarta.ws.rs.core.Response.Status.CREATED;

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

@Path("/quarkus-azure-storage-blob")
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
    @Produces(MediaType.TEXT_PLAIN)
    public Response downloadBlob(
            @PathParam("container") String container,
            @PathParam("blobName") String blobName) {
        BlobContainerClient blobContainerClient = blobServiceClient.createBlobContainerIfNotExists(container);
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        if (blobClient.exists()) {
            return Response.ok().entity(blobClient.downloadContent().toString()).build();
        } else {
            return Response.status(404).build();
        }

    }

    @Path("/{container}/{blobName}")
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public Response delete(
            @PathParam("container") String container,
            @PathParam("blobName") String blobName) {
        BlobContainerClient blobContainerClient = blobServiceClient
                .createBlobContainerIfNotExists(container);
        blobContainerClient.getBlobClient(blobName).delete();

        return Response.noContent().build();
    }

    @Path("/{container}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String listBlobs(@PathParam("container") String container) {
        BlobContainerClient blobContainerClient = blobServiceClient
                .createBlobContainerIfNotExists(container);
        return blobContainerClient.listBlobs().stream()
                .map(BlobItem::getName)
                .collect(Collectors.joining(","));
    }

}
