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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;

@Path("/quarkus-azure-key-vault-secret-disabled")
@Produces(MediaType.TEXT_PLAIN)
@ApplicationScoped
public class KeyVaultSecretDisabledResource {

    @Inject
    SecretClient secretClient;

    @Inject
    SecretAsyncClient secretAsyncClient;

    @Path("/secretClient")
    @GET
    public String getSecretClient() {
        assert secretClient == null : "The SecretClient should be null";
        return "The SecretClient is null because the Azure Key Vault secret is disabled";
    }

    @Path("/secretAsyncClient")
    @GET
    public String getSecretAsyncClient() {
        assert secretAsyncClient == null : "The SecretAsyncClient should be null";
        return "The SecretAsyncClient is null because the Azure Key Vault secret is disabled";
    }
}
