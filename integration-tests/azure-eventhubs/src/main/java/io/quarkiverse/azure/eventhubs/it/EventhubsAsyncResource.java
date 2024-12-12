package io.quarkiverse.azure.eventhubs.it;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.messaging.eventhubs.*;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionContext;

import reactor.core.Disposable;

@Path("/quarkus-azure-eventhubs-async")
@ApplicationScoped
public class EventhubsAsyncResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventhubsAsyncResource.class);

    @Inject
    EventHubProducerAsyncClient producer;

    @Inject
    EventHubConsumerAsyncClient consumer;

    @Path("/publishEvents")
    @GET
    public void publishEvents() {
        List<EventData> allEvents = List.of(new EventData("Foo-Asyn"), new EventData("Bar-Asyn"));
        // Creating a batch without options set, will allow for automatic routing of events to any partition.
        producer.createBatch().flatMap(batch -> {
            allEvents.forEach(batch::tryAdd);
            return producer.send(batch);
        }).subscribe(unused -> {
        },
                error -> LOGGER.error("Error occurred while publishing events: {}", error),
                () -> LOGGER.info("Completed publishing events."));
    }

    @Path("/consumeEvents")
    @GET
    public void consumeEvents() {

        // Obtain partitionId from EventHubConsumerAsyncClient.getPartitionIds()
        String partitionId = "0";
        EventPosition startingPosition = EventPosition.latest();

        Disposable subscription = consumer.receiveFromPartition(partitionId, startingPosition)
                .subscribe(partitionEvent -> {
                    PartitionContext partitionContext = partitionEvent.getPartitionContext();
                    EventData event = partitionEvent.getData();

                    LOGGER.info("Received event from partition '{}'", partitionContext.getPartitionId());
                    LOGGER.info("Contents of event: {}", event.getBodyAsString());
                }, error -> {
                    // This is a terminal signal.  No more events will be received from the same Flux object.
                    LOGGER.error("Error occurred while consuming events: {}", error);
                }, () -> {
                    LOGGER.info("Completed receiving events.");
                });
    }
}
