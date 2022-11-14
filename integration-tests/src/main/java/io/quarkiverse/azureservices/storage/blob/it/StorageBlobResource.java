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
package io.quarkiverse.azureservices.storage.blob.it;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/azure-storage-blob")
@ApplicationScoped
public class StorageBlobResource {

    @Inject
    BlobServiceClient blobServiceClient;

    @GET
    public String hello() {
        BlobContainerClient blobContainerClient = blobServiceClient.createBlobContainerIfNotExists("mycontainer");
        BlobClient blobClient = blobContainerClient.getBlobClient("myblob");
        blobClient.upload(BinaryData.fromString("Hello azure-storage-blob"), true);

        return blobClient.downloadContent().toString();
    }
}
