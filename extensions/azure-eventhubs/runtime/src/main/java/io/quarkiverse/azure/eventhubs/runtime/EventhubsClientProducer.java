package io.quarkiverse.azure.eventhubs.runtime;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;

public class EventhubsClientProducer {

    @Inject
    EventhubsConfig eventhubsConfig;

    @Produces
    public EventHubProducerClient createEventHubProducerClient() {
        if (!eventhubsConfig.enabled()) {
            return null;
        }

        EventHubClientBuilder builder = getBuilder();

        return null == builder ? null : builder.buildProducerClient();
    }

    @Produces
    public EventHubProducerAsyncClient createEventHubProducerAsyncClient() {
        if (!eventhubsConfig.enabled()) {
            return null;
        }

        EventHubClientBuilder builder = getBuilder();

        return null == builder ? null : builder.buildAsyncProducerClient();
    }

    @Produces
    public EventHubConsumerClient createEventHubConsumerClient() {
        if (!eventhubsConfig.enabled()) {
            return null;
        }

        EventHubClientBuilder builder = getBuilder();

        return null == builder ? null
                : builder.consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                        .buildConsumerClient();
    }

    @Produces
    public EventHubConsumerAsyncClient createEventhubClient() {
        if (!eventhubsConfig.enabled()) {
            return null;
        }

        EventHubClientBuilder builder = getBuilder();

        return null == builder ? null
                : builder.consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                        .buildAsyncConsumerClient();
    }

    private EventHubClientBuilder getBuilder() {
        if (!eventhubsConfig.enabled()) {
            return null;
        }

        EventHubClientBuilder builder = new EventHubClientBuilder()
                .credential(eventhubsConfig.namespace()
                        + "."
                        + eventhubsConfig.domainName(),
                        eventhubsConfig.eventhubName(),
                        new DefaultAzureCredentialBuilder().build());
        return builder;
    }
}
