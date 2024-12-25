package io.quarkiverse.azure.services.disabled.it;

import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

@Path("/quarkus-eventhubs-disabled")
@Produces(MediaType.TEXT_PLAIN)
@ApplicationScoped
public class EventHubsDisabledResource {

    @Inject
    EventHubProducerClient eventHubProducerClient;

    @Inject
    EventHubConsumerClient eventHubConsumerClient;

    @Inject
    EventHubProducerAsyncClient eventHubProducerAsyncClient;

    @Inject
    EventHubConsumerAsyncClient eventHubConsumerAsyncClient;

    @Path("/eventHubProducerClient")
    @GET
    public Response getEventHubProducerClient() {
        assert eventHubProducerClient == null : "The EventHubProducerClient should be null";
        return Response.status(NOT_FOUND).entity("The EventHubProducerClient is null because the Azure Event Hubs is disabled")
                .build();
    }

    @Path("/eventHubConsumerClient")
    @GET
    public Response getEventHubConsumerClient() {
        assert eventHubConsumerClient == null : "The EventHubConsumerClient should be null";
        return Response.status(NOT_FOUND).entity("The EventHubConsumerClient is null because the Azure Event Hubs is disabled")
                .build();
    }

    @Path("/eventHubProducerAsyncClient")
    @GET
    public Response getEventHubProducerAsyncClient() {
        assert eventHubProducerAsyncClient == null : "The EventHubProducerAsyncClient should be null";
        return Response.status(NOT_FOUND)
                .entity("The EventHubProducerAsyncClient is null because the Azure Event Hubs is disabled")
                .build();
    }

    @Path("/eventHubConsumerAsyncClient")
    @GET
    public Response getEventHubConsumerAsyncClient() {
        assert eventHubConsumerAsyncClient == null : "The EventHubConsumerAsyncClient should be null";
        return Response.status(NOT_FOUND)
                .entity("The EventHubConsumerAsyncClient is null because the Azure Event Hubs is disabled")
                .build();
    }
}
