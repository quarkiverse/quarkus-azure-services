package io.quarkiverse.azure.eventhubs.it;

import java.util.Arrays;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.jboss.logging.Logger;

import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.SendOptions;

@Path("/quarkus-azure-eventhubs")
@ApplicationScoped
public class EventhubsResource {
    private static final Logger LOGGER = Logger.getLogger(EventhubsResource.class);

    @Inject
    EventHubProducerClient producer;

    @Inject
    EventHubConsumerClient consumer;

    @Path("/sendEvents")
    @GET
    public void sendEvents() {
        List<EventData> allEvents = Arrays.asList(new EventData("Foo"), new EventData("Bar"));
        producer.send(allEvents, new SendOptions().setPartitionId("0"));
    }

    @Path("/receiveEvents")
    @GET
    public void receiveEvents() {

        LOGGER.info("Receiving message using Event Hub consumer client.");
        String PARTITION_ID = "0";
        // Reads events from partition '0' and returns the first 2 received.
        IterableStream<PartitionEvent> events = consumer.receiveFromPartition(PARTITION_ID, 2,
                EventPosition.earliest());

        for (PartitionEvent partitionEvent : events) {
            // For each event, perform some sort of processing.
            LOGGER.info("Message Body received: " + partitionEvent.getData().getBodyAsString());
            LOGGER.info("Message SequenceNumber is: " + partitionEvent.getData().getSequenceNumber());
        }
    }
}
