package io.quarkiverse.azure.services.disabled.it;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;

@Path("/quarkus-azure-cosmos-disabled")
@Produces(MediaType.TEXT_PLAIN)
@ApplicationScoped
public class CosmosDisabledResource {

    @Inject
    CosmosClient cosmosClient;

    @Inject
    CosmosAsyncClient cosmosAsyncClient;

    @Path("/cosmosClient")
    @GET
    public Response getCosmosClient() {
        assert cosmosClient == null : "The CosmosClient should be null";
        return Response.status(NOT_FOUND).entity("The CosmosClient is null because the Azure Cosmos DB is disabled")
                .build();
    }

    @Path("/cosmosAsyncClient")
    @GET
    public Response getCosmosAsyncClient() {
        assert cosmosAsyncClient == null : "The CosmosAsyncClient should be null";
        return Response.status(NOT_FOUND).entity("The CosmosAsyncClient is null because the Azure Cosmos DB is disabled")
                .build();
    }
}
