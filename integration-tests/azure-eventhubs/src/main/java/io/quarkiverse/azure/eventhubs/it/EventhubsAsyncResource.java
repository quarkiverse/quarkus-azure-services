package io.quarkiverse.azure.eventhubs.it;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.SendOptions;

@Path("/quarkus-azure-eventhubs-async")
@ApplicationScoped
public class EventhubsAsyncResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventhubsAsyncResource.class);

    @Inject
    EventHubProducerAsyncClient producer;

    @Inject
    EventHubConsumerAsyncClient consumer;

    @Path("/sendEvents")
    @GET
    public void sendEvents() throws InterruptedException {
        List<EventData> allEvents = List.of(new EventData("Foo-Asyn"), new EventData("Bar-Asyn"));
        // Creating a batch without options set, will allow for automatic routing of events to any partition.
        producer.send(allEvents, new SendOptions().setPartitionId("1"))
                .subscribe(unused -> {
                    LOGGER.info("Event sent successfully.");
                }, error -> {
                    LOGGER.error("Error occurred while sending events: {}", error);
                });
        // Wait for the send to complete.
        Thread.sleep(5000);

    }

    @Path("/receiveEvents")
    @GET
    public void receiveEvents() throws InterruptedException {

        String partitionId = "1";

        consumer.receiveFromPartition(partitionId, EventPosition.earliest())
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
        // Wait for the receive to complete.
        Thread.sleep(5000);

    }
}
