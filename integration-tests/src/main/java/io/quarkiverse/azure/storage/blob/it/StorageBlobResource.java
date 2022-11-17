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

import java.time.LocalDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;

@Path("/quarkus-azure-storage-blob")
@ApplicationScoped
public class StorageBlobResource {

    @Inject
    BlobServiceClient blobServiceClient;

    @POST
    public Response uploadBlob() {
        BlobContainerClient blobContainerClient = blobServiceClient
                .createBlobContainerIfNotExists("container-quarkus-azure-storage-blob");
        BlobClient blobClient = blobContainerClient.getBlobClient("quarkus-azure-storage-blob.txt");
        blobClient.upload(BinaryData.fromString("Hello quarkus-azure-storage-blob at " + LocalDateTime.now()), true);

        return Response.status(CREATED).build();
    }

    @GET
    public String downloadBlob() {
        BlobContainerClient blobContainerClient = blobServiceClient
                .createBlobContainerIfNotExists("container-quarkus-azure-storage-blob");
        BlobClient blobClient = blobContainerClient.getBlobClient("quarkus-azure-storage-blob.txt");

        return blobClient.downloadContent().toString();
    }
}
