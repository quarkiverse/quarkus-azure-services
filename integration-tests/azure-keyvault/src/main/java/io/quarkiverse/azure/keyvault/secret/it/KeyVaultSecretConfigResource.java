package io.quarkiverse.azure.keyvault.secret.it;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/keyvaultConfig")
public class KeyVaultSecretConfigResource {
    @ConfigProperty(name = "kv//secret1")
    String value;

    @ConfigProperty(name = "kv//notExistingSecret")
    String notExistingSecret;

    @GET
    @Path("getExistingSecret")
    @Produces(TEXT_PLAIN)
    public String getExistingSecret() {
        return value;
    }

    @GET
    @Path("getNotExistingSecret")
    @Produces(TEXT_PLAIN)
    public String getNotExistingSecret() {
        return notExistingSecret;
    }
}
