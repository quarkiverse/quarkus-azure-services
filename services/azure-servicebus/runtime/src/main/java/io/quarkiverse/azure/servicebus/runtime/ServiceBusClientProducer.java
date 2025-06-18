package io.quarkiverse.azure.servicebus.runtime;

import static io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig.CONFIG_KEY_CONNECTION_STRING;
import static io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig.CONFIG_KEY_ENABLED;
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
                .orElseThrow(() -> new ConfigurationException(String.format(
                        "Either the connection string (%s) or the namespace (%s) must be set.\n" +
                                "Alternatively, you can disable the CDI producers of the Azure Service Bus extension with '%s=false'.",
                        CONFIG_KEY_CONNECTION_STRING, CONFIG_KEY_NAMESPACE, CONFIG_KEY_ENABLED)));

        return new ServiceBusClientBuilder()
                .fullyQualifiedNamespace(namespace + "." + config.domainName())
                .credential(new DefaultAzureCredentialBuilder().build());
    }
}
