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

        String namespace = config.namespace()
                .orElseThrow(ServiceBusConfigVerifier::missingConnectionConfigurationException);

        return new ServiceBusClientBuilder()
                .fullyQualifiedNamespace(namespace + "." + config.domainName())
                .credential(new DefaultAzureCredentialBuilder().build());
    }
}
