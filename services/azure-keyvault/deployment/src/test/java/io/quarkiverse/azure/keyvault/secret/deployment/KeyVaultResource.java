package io.quarkiverse.azure.keyvault.secret.deployment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

@Path("/key-vault-secrets")
@ApplicationScoped
public class KeyVaultResource {

    record SecretItem(String name, String value) {
    }

    @Inject
    SecretClient secretClient;

    @ConfigProperty(name = "kv//secret1")
    String secret;

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createItem(
            SecretItem body,
            @Context UriInfo uriInfo) {
        KeyVaultSecret setSecret = secretClient.setSecret(body.name(), body.value());
        String relative = body.name() + "/version/" + setSecret.getProperties().getVersion();
        return Response.created(uriInfo.getAbsolutePathBuilder().path(relative).build()).build();
    }

    @Path("/{name}/version/{version}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public Response readItem(
            @PathParam("name") String name,
            @PathParam("version") String version) {
        KeyVaultSecret vaultSecret = secretClient.getSecret(name, version);
        return Response.ok(new SecretItem(vaultSecret.getName(), vaultSecret.getValue())).build();
    }

    @Path("/injected")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public Response readInjectedSecret() {
        return Response.ok(new SecretItem("secret1", secret)).build();
    }
}
