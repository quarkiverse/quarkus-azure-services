package io.quarkiverse.azure.servicebus.runtime;

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
                        "Either the connection string (quarkus.azure.servicebus.connection-string) or the namespace (quarkus.azure.servicebus.namespace) must be set."));

        return new ServiceBusClientBuilder()
                .fullyQualifiedNamespace(namespace + "." + config.domainName())
                .credential(new DefaultAzureCredentialBuilder().build());
    }
}
