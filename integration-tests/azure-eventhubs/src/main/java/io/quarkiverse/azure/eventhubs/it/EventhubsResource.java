package io.quarkiverse.azure.eventhubs.it;

import java.util.Arrays;
import java.util.List;

import com.azure.messaging.eventhubs.models.SendOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;

@Path("/quarkus-azure-eventhubs")
@ApplicationScoped
public class EventhubsResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventhubsResource.class);

    @Inject
    EventHubProducerClient producer;

    @Inject
    EventHubConsumerClient consumer;

    @Path("/publishEvents")
    @GET
    public void publishEvents() {

        List<EventData> allEvents = Arrays.asList(new EventData("Foo"), new EventData("Bar"));
        EventDataBatch eventDataBatch = producer.createBatch();

        producer.send(allEvents, new SendOptions().setPartitionId("0"));

        // Clients are expected to be long-lived objects.
        // Dispose of the producer to close any underlying resources when we are finished with it.
        producer.close();

    }

    @Path("/consumeEvents")
    @GET
    public void consumeEvents() {

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

        producer.close();
    }
}
