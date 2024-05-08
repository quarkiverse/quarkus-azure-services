package io.quarkiverse.azure.services.disabled.it;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.smallrye.config.SmallRyeConfig;

@Path("/azure-app-config-disabled")
@Produces(MediaType.APPLICATION_JSON)
public class AzureAppConfigDisabledResource {
    @Inject
    SmallRyeConfig config;

    @GET
    @Path("/{name}")
    public String getValue(@PathParam("name") final String name) {
        String value = config.getConfigValue(name).getValue();
        assert value == null;
        return "Azure App Configuration is disabled";
    }

}
