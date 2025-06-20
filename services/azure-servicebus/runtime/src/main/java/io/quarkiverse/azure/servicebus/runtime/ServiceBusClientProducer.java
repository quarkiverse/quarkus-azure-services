package io.quarkiverse.azure.servicebus.runtime;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;

public class ServiceBusClientProducer {

    @Inject
    ServiceBusConfig config;

    @Produces
    ServiceBusClientBuilder produceServiceBusClientBuilder() {
        if (config.connectionString().isPresent()) {
            return new ServiceBusClientBuilder()
                    .connectionString(config.connectionString().get());
        }

        // Configuration was already verified in ServiceBusConfigVerifier,
        // so we can rely on the namespace being present here.
        String namespace = config.namespace().get();

        return new ServiceBusClientBuilder()
                .fullyQualifiedNamespace(namespace + "." + config.domainName())
                .credential(new DefaultAzureCredentialBuilder().build());
    }
}
