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
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlockBlobItem;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Path("/quarkus-azure-storage-blob-async")
@Produces(MediaType.TEXT_PLAIN)
@ApplicationScoped
public class StorageBlobAsyncResource {

    @Inject
    BlobServiceAsyncClient blobServiceAsyncClient;

    @Path("/{container}/{blobName}")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public Uni<Response> uploadBlob(
            @PathParam("container") String container,
            @PathParam("blobName") String blobName,
            String body) {
        Mono<BlockBlobItem> blockBlobItem = blobServiceAsyncClient.createBlobContainerIfNotExists(container)
                .map(it -> it.getBlobAsyncClient(blobName))
                .flatMap(it -> it.upload(BinaryData.fromString(body), true));

        return Uni.createFrom().completionStage(blockBlobItem.toFuture()).map(it -> Response.status(CREATED).build());
    }

    @Path("/{container}/{blobName}")
    @HEAD
    public Uni<Response> blobExists(
            @PathParam("container") String container,
            @PathParam("blobName") String blobName) {
        BlobAsyncClient blobAsyncClient = blobServiceAsyncClient.getBlobContainerAsyncClient(container)
                .getBlobAsyncClient(blobName);

        return Uni.createFrom().completionStage(blobAsyncClient.exists().map(it -> {
            if (Boolean.TRUE.equals(it)) {
                return Response.ok().build();
            } else {
                return Response.status(NOT_FOUND).build();
            }
        }).toFuture());
    }

    @Path("/{container}/{blobName}")
    @GET
    public Uni<Response> downloadBlob(
            @PathParam("container") String container,
            @PathParam("blobName") String blobName) {
        BlobAsyncClient blobAsyncClient = blobServiceAsyncClient.getBlobContainerAsyncClient(container)
                .getBlobAsyncClient(blobName);

        return Uni.createFrom()
                .completionStage(blobAsyncClient.downloadContent().map(it -> Response.ok().entity(it.toString()).build())
                        .toFuture());
    }

    @Path("/{container}/{blobName}")
    @DELETE
    public Uni<Response> delete(
            @PathParam("container") String container,
            @PathParam("blobName") String blobName) {
        Mono<Void> deleteBlob = blobServiceAsyncClient.getBlobContainerAsyncClient(container)
                .getBlobAsyncClient(blobName).delete();

        return Uni.createFrom().completionStage(deleteBlob.toFuture()).map(it -> Response.noContent().build());
    }

    @Path("/{container}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<String> listBlobs(@PathParam("container") String container) {
        Flux<String> flux = blobServiceAsyncClient.getBlobContainerAsyncClient(container)
                .listBlobs()
                .map(BlobItem::getName);
        return Multi.createFrom().emitter(emitter -> {
            flux.subscribe(emitter::emit, emitter::fail, emitter::complete);
        });
    }
}
