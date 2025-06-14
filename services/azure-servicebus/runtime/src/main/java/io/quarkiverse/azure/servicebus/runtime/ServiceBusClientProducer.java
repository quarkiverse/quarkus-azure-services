package io.quarkiverse.azure.servicebus.runtime;

import static io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig.CONFIG_KEY_CONNECTION_STRING;
import static io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig.CONFIG_KEY_NAMESPACE;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;

import io.quarkus.runtime.configuration.ConfigurationException;

public class ServiceBusClientProducer {

    @Inject
    ServiceBusConfig config;

    @Produces
    ServiceBusClientBuilder produceServiceBusClientBuilder() {
        if (config.connectionString().isPresent()) {
            return new ServiceBusClientBuilder()
                    .connectionString(config.connectionString().get());
        }

        String namespace = config.namespace()
                .orElseThrow(() -> new ConfigurationException(
                        "Either the connection string (%s) or the namespace (%s) must be set."
                                .formatted(CONFIG_KEY_CONNECTION_STRING, CONFIG_KEY_NAMESPACE)));

        return new ServiceBusClientBuilder()
                .fullyQualifiedNamespace(namespace + "." + config.domainName())
                .credential(new DefaultAzureCredentialBuilder().build());
    }
}
