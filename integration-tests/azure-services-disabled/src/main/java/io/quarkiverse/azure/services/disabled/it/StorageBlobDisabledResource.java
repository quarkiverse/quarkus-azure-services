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
