package io.quarkiverse.azure.app.configuration.it;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.smallrye.config.SmallRyeConfig;

@Path("/config")
@Produces(MediaType.APPLICATION_JSON)
public class ConfigResource {
    @Inject
    SmallRyeConfig config;

    @GET
    @Path("/{name}")
    public String configValue(@PathParam("name") final String name) {
        return config.getConfigValue(name).getValue();
    }

}
