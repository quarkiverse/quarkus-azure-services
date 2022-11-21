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

import static javax.ws.rs.core.Response.Status.CREATED;

import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    public String listBlobs() {
        BlobContainerClient blobContainerClient = blobServiceClient
                .createBlobContainerIfNotExists("container-quarkus-azure-storage-blob");
        return blobContainerClient.listBlobs().stream()
                .map(BlobItem::getName)
                .collect(Collectors.joining(","));
    }

}
