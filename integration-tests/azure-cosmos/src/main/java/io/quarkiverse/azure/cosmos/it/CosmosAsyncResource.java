package io.quarkiverse.azure.cosmos.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Path("/quarkus-azure-cosmos-async")
@ApplicationScoped
public class CosmosAsyncResource {

    @Inject
    CosmosAsyncClient cosmosAsyncClient;

    @Path("/{database}/{container}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> createItem(
            @PathParam("database") String database,
            @PathParam("container") String container,
            Item body,
            @Context UriInfo uriInfo) {

        Mono<CosmosItemResponse<Item>> response = getContainer(database, container, true)
                .flatMap(cosmosAsyncContainer -> cosmosAsyncContainer.upsertItem(body));
        return Uni.createFrom().completionStage(response.toFuture())
                .map(it -> Response.created(uriInfo.getAbsolutePathBuilder().path(body.getId()).build()).build());
    }

    @Path("/{database}/{container}/{itemId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getItem(
            @PathParam("database") String database,
            @PathParam("container") String container,
            @PathParam("itemId") String itemId) {
        Mono<CosmosItemResponse<Item>> item = getContainer(database, container, false)
                .flatMap(cosmosAsyncContainer -> cosmosAsyncContainer.readItem(itemId, new PartitionKey(itemId), Item.class));
        return Uni.createFrom().completionStage(item.toFuture())
                .map(it -> Response.ok().entity(it.getItem()).build());

    }

    @Path("/{database}/{container}/{itemId}")
    @DELETE
    public Uni<Response> deleteItem(
            @PathParam("database") String database,
            @PathParam("container") String container,
            @PathParam("itemId") String itemId) {
        Mono<CosmosItemResponse<Object>> response = getContainer(database, container, false)
                .flatMap(cosmosAsyncContainer -> cosmosAsyncContainer.deleteItem(itemId, new PartitionKey(itemId),
                        new CosmosItemRequestOptions()));
        return Uni.createFrom().completionStage(response.toFuture())
                .map(it -> Response.noContent().build());
    }

    @Path("/{database}/{container}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Item> getItems(
            @PathParam("database") String database,
            @PathParam("container") String container) {
        Flux<Item> items = getContainer(database, container, false)
                .map(cosmosAsyncContainer -> cosmosAsyncContainer
                        .queryItems("SELECT * FROM Item", new CosmosQueryRequestOptions(), Item.class)
                        .byPage(10)
                        .map(FeedResponse::getResults)
                        .flatMapIterable(it -> it))
                .flatMapMany(flux -> flux);

        return Multi.createFrom().emitter(emitter -> {
            items.subscribe(emitter::emit, emitter::fail, emitter::complete);
        });
    }

    private Mono<CosmosAsyncContainer> getContainer(String database, String container, boolean createIfNotExists) {
        if (!createIfNotExists) {
            return Mono.just(cosmosAsyncClient.getDatabase(database).getContainer(container));
        }

        return cosmosAsyncClient.createDatabaseIfNotExists(database)
                .map(CosmosDatabaseResponse::getProperties)
                .map(CosmosDatabaseProperties::getId)
                .map(cosmosAsyncClient::getDatabase)
                .flatMap(databaseAsync -> databaseAsync.createContainerIfNotExists(container, Item.PARTITION_KEY))
                .map(CosmosContainerResponse::getProperties)
                .map(CosmosContainerProperties::getId)
                .map(id -> cosmosAsyncClient.getDatabase(database).getContainer(container));
    }
}
