package io.quarkiverse.azure.eventhubs.runtime;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class EventhubsClientProducer {

    @Inject
    EventhubsConfig eventhubsConfiguration;

    @Produces
    public EventHubProducerClient createEventHubProducerClient() {
        EventHubClientBuilder builder = getBuilder();

        return null == builder ? null : builder.buildProducerClient();
    }

    @Produces
    public EventHubProducerAsyncClient createEventHubProducerAsyncClient() {
        EventHubClientBuilder builder = getBuilder();

        return null == builder ? null : builder.buildAsyncProducerClient();
    }

    @Produces
    public EventHubConsumerClient createEventHubConsumerClient() {
        EventHubClientBuilder builder = getBuilder();

        return null == builder ? null : builder.consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .buildConsumerClient();
    }

    @Produces
    public EventHubConsumerAsyncClient createEventhubClient() {
        EventHubClientBuilder builder = getBuilder();

        return null == builder ? null : builder.consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .buildAsyncConsumerClient();
    }

    private EventHubClientBuilder getBuilder() {
        if (!eventhubsConfiguration.enabled()) {
            return null;
        }

        assert eventhubsConfiguration.endpoint().isPresent() : "The endpoint of Azure Eventhubs must be set";
        EventHubClientBuilder builder = new EventHubClientBuilder()
                .credential(eventhubsConfiguration.fullyQualifiedNamespace(),
                        eventhubsConfiguration.eventHubName(),
                        new DefaultAzureCredentialBuilder().build());
        return builder;
    }
}
