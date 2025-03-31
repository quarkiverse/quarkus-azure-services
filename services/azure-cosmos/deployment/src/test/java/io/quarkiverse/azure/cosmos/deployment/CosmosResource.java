package io.quarkiverse.azure.cosmos.deployment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import com.azure.cosmos.CosmosClient;

@Path("/cosmos")
@ApplicationScoped
public class CosmosResource {

    record Item(String id, String name) {
    }

    @Inject
    CosmosClient cosmosClient;

    @Path("/{database}/{container}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createItem(
            @PathParam("database") String database,
            @PathParam("container") String container,
            Item body,
            @Context UriInfo uriInfo) {
        cosmosClient.createDatabaseIfNotExists(database);
        cosmosClient.getDatabase(database).createContainerIfNotExists(container, "/id");
        cosmosClient.getDatabase(database)
                .getContainer(container)
                .upsertItem(body);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(body.id()).build()).build();
    }
}
