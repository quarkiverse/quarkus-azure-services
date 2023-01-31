package io.quarkiverse.azure.app.configuration.pt;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
