package io.quarkiverse.azure.eventhubs.it;

import java.util.Arrays;
import java.util.List;

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

        for (EventData eventData : allEvents) {
            // try to add the event from the array to the batch
            if (!eventDataBatch.tryAdd(eventData)) {
                // if the batch is full, send it and then create a new batch
                producer.send(eventDataBatch);
                eventDataBatch = producer.createBatch();

                // Try to add that event that couldn't fit before.
                if (!eventDataBatch.tryAdd(eventData)) {
                    throw new IllegalArgumentException("Event is too large for an empty batch. Max size: "
                            + eventDataBatch.getMaxSizeInBytes());
                }
            }
        }

        // send the last batch of remaining events
        if (eventDataBatch.getCount() > 0) {
            producer.send(eventDataBatch);
        }

        // Clients are expected to be long-lived objects.
        // Dispose of the producer to close any underlying resources when we are finished with it.
        producer.close();

    }

    @Path("/consumeEvents")
    @GET
    public void consumeEvents() {

        LOGGER.info("Receiving message using Event Hub consumer client.");
        String PARTITION_ID = "0";
        // Reads events from partition '0' and returns the first 2 received .
        IterableStream<PartitionEvent> events = consumer.receiveFromPartition(PARTITION_ID, 2,
                EventPosition.earliest());

        Long lastSequenceNumber = -1L;
        for (PartitionEvent partitionEvent : events) {
            // For each event, perform some sort of processing.
            LOGGER.info("Message received: " + partitionEvent.getData().getBodyAsString());
            LOGGER.info("SequenceNumber is: " + partitionEvent.getData().getSequenceNumber());
        }
    }

}
