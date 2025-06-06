package io.quarkiverse.azure.eventhubs.runtime;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.azure.core.util.ClientOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;

import io.quarkiverse.azure.core.util.AzureQuarkusIdentifier;
import io.quarkus.runtime.configuration.ConfigurationException;

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

        String namespace = eventhubsConfig.namespace()
                .orElseThrow(() -> new ConfigurationException(
                        "The namespace of Azure Event Hubs (quarkus.azure.eventhubs.namespace) must be set"));
        String domainName = eventhubsConfig.domainName()
                .orElseThrow(() -> new ConfigurationException(
                        "The domain name of Azure Event Hubs (quarkus.azure.eventhubs.domain-name) must be set"));
        String eventhubName = eventhubsConfig.eventhubName()
                .orElseThrow(() -> new ConfigurationException(
                        "The event hub name of Azure Event Hubs (quarkus.azure.eventhubs.eventhub-name) must be set"));
        return new EventHubClientBuilder()
                .clientOptions(new ClientOptions().setApplicationId(AzureQuarkusIdentifier.AZURE_QUARKUS_EVENTHUBS))
                .credential(namespace + "." + domainName,
                        eventhubName,
                        new DefaultAzureCredentialBuilder().build());
    }
}
