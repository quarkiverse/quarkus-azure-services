package io.quarkiverse.azure.services.together.it;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.azure.security.keyvault.secrets.SecretClient;

@Path("/quarkus-services-azure-key-vault")
@Produces(TEXT_PLAIN)
public class KeyVaultSecretResource {
    private static final Logger LOG = Logger.getLogger(KeyVaultSecretResource.class);

    @Inject
    SecretClient secretClient;

    @GET
    @Path("getSecretBySecretClient")
    public String testSecretClient(String value) {
        LOG.info("Testing SecretClient by getting value of secret secret1");
        return secretClient.getSecret("secret1").getValue();
    }

    @ConfigProperty(name = "kv//secret1")
    String value;

    @GET
    @Path("getSecretByConfigProperty")
    public String getConfigProperty() {
        LOG.info("Testing ConfigProperty by getting value of secret secret1");
        return value;
    }
}
