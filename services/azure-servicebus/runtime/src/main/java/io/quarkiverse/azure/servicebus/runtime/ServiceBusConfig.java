package io.quarkiverse.azure.servicebus.runtime;

import static io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig.PREFIX;
import static io.quarkus.runtime.annotations.ConfigPhase.RUN_TIME;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = PREFIX)
@ConfigRoot(phase = RUN_TIME)
public interface ServiceBusConfig {
    String PREFIX = "quarkus.azure.servicebus";
    String ENABLED = PREFIX + ".enabled";
    String CONNECTION_STRING = PREFIX + ".connection-string";
    String NAMESPACE = PREFIX + ".namespace";
    String DOMAIN_NAME = PREFIX + ".domain-name";

    /**
     * Connect to the Service Bus using this connection string.
     * If set, authentication is handled by the SAS key in the connection string.
     * Otherwise, a DefaultAzureCredentialBuilder will be used for authentication,
     * and namespace and domain have to be configured.
     */
    Optional<String> connectionString();

    /**
     * The namespace of the Service Bus.
     */
    Optional<String> namespace();

    /**
     * The domain name of the Service Bus.
     */
    @WithDefault("servicebus.windows.net")
    String domainName();
}
