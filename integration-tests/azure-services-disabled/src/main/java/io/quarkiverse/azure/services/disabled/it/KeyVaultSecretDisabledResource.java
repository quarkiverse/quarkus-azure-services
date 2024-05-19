package io.quarkiverse.azure.services.disabled.it;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
    public Response getSecretClient() {
        assert secretClient == null : "The SecretClient should be null";
        return Response.status(NOT_FOUND).entity("The SecretClient is null because the Azure Key Vault secret is disabled")
                .build();
    }

    @Path("/secretAsyncClient")
    @GET
    public Response getSecretAsyncClient() {
        assert secretAsyncClient == null : "The SecretAsyncClient should be null";
        return Response.status(NOT_FOUND).entity("The SecretAsyncClient is null because the Azure Key Vault secret is disabled")
                .build();
    }
}
