package io.quarkiverse.azure.services.disabled.it;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.smallrye.config.SmallRyeConfig;

@Path("/quarkus-azure-app-config-disabled")
@Produces(MediaType.TEXT_PLAIN)
public class AzureAppConfigDisabledResource {
    @Inject
    SmallRyeConfig config;

    @GET
    @Path("/{name}")
    public String getValue(@PathParam("name") final String name) {
        assert config.getConfigValue(name).getValue() == null : "The value should be null";
        return "The value is null because the Azure App Configuration is disabled";
    }

}
