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
