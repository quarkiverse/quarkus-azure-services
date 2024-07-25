package io.quarkiverse.azure.cosmos.it;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.*;

@Path("/quarkus-azure-cosmos")
@Produces(MediaType.TEXT_PLAIN)
@ApplicationScoped
public class CosmosResource {

    @Inject
    CosmosClient cosmosClient;

    @Path("/{database}/{container}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createItem(
            @PathParam("database") String database,
            @PathParam("container") String container,
            Item body) {

        getContainer(database, container).upsertItem(body);

        return Response.ok(URI.create("/" + database + "/" + container + "/" + body.getId())).build();
    }

    @Path("/{database}/{container}/{itemId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getItem(
            @PathParam("database") String database,
            @PathParam("container") String container,
            @PathParam("itemId") String itemId) {
        CosmosItemResponse<Item> item = getContainer(database, container).readItem(itemId, new PartitionKey(itemId),
                Item.class);

        return Response.ok().entity(item.getItem()).build();

    }

    @Path("/{database}/{container}/{itemId}")
    @DELETE
    public Response deleteItem(
            @PathParam("database") String database,
            @PathParam("container") String container,
            @PathParam("itemId") String itemId) {
        getContainer(database, container).deleteItem(itemId, new PartitionKey(itemId), new CosmosItemRequestOptions());

        return Response.noContent().build();
    }

    @Path("/{database}/{container}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Item> getItems(
            @PathParam("database") String database,
            @PathParam("container") String container) {
        return getContainer(database, container)
                .queryItems("SELECT * FROM Item", new CosmosQueryRequestOptions(), Item.class)
                .streamByPage(10)
                .map(FeedResponse::getResults)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private CosmosContainer getContainer(String database, String container) {
        CosmosDatabaseResponse databaseResponse = cosmosClient.createDatabaseIfNotExists(database);
        CosmosDatabase cosmosDatabase = cosmosClient.getDatabase(databaseResponse.getProperties().getId());
        CosmosContainerResponse containerResponse = cosmosDatabase.createContainerIfNotExists(container, Item.PARTITION_KEY);
        return cosmosDatabase.getContainer(containerResponse.getProperties().getId());
    }

}
