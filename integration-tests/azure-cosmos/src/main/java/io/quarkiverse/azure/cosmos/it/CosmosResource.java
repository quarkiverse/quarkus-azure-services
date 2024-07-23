package io.quarkiverse.azure.cosmos.it;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

import java.net.URI;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;

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

        getContainerForItem(database, container).createItem(body);

        return Response.created(URI.create("/" + database + "/" + container + "/" + body.getId())).build();
    }

    @Path("/{database}/{container}/{itemId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getItem(
            @PathParam("database") String database,
            @PathParam("container") String container,
            @PathParam("itemId") String itemId) {
        CosmosItemResponse<Item> item = getContainerForItem(database, container).readItem(itemId, new PartitionKey(itemId),
                Item.class);

        return Response.ok().entity(item.getItem()).build();

    }

    @Path("/{database}/{container}/{itemId}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateItem(
            @PathParam("database") String database,
            @PathParam("container") String container,
            @PathParam("itemId") String itemId,
            Item body) {
        CosmosContainer cosmosContainer = getContainerForItem(database, container);
        CosmosItemResponse<Item> item = cosmosContainer.readItem(itemId, new PartitionKey(itemId), Item.class);
        if (item == null) {
            return Response.status(NOT_FOUND).build();
        }

        cosmosContainer.replaceItem(body, itemId, new PartitionKey(itemId), new CosmosItemRequestOptions());
        return Response.ok().build();
    }

    @Path("/{database}/{container}/{itemId}")
    @DELETE
    public Response deleteItem(
            @PathParam("database") String database,
            @PathParam("container") String container,
            @PathParam("itemId") String itemId) {
        getContainerForItem(database, container).deleteItem(itemId, new PartitionKey(itemId), new CosmosItemRequestOptions());

        return Response.noContent().build();
    }

    @Path("/{database}/{container}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CosmosPagedIterable<Item> getItems(
            @PathParam("database") String database,
            @PathParam("container") String container) {
        return getContainerForItem(database, container).readAllItems(new PartitionKey(Item.PARTITION_KEY), Item.class);
    }

    private CosmosContainer getContainerForItem(String database, String container) {
        CosmosDatabaseResponse databaseResponse = cosmosClient.createDatabaseIfNotExists(database);
        CosmosDatabase cosmosDatabase = cosmosClient.getDatabase(databaseResponse.getProperties().getId());
        CosmosContainerResponse containerResponse = cosmosDatabase.createContainerIfNotExists(container, Item.PARTITION_KEY);
        return cosmosDatabase.getContainer(containerResponse.getProperties().getId());
    }

}
