package io.quarkiverse.azure.servicebus.runtime;

import static io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig.CONFIG_KEY_CONNECTION_STRING;
import static io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig.CONFIG_KEY_ENABLED;
import static io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig.CONFIG_KEY_NAMESPACE;

import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.Startup;
import io.quarkus.runtime.configuration.ConfigurationException;

/**
 * Due to lazy initialization of injection points, the CDI producer method of this extension
 * might be called at application runtime.
 * <p>
 * We do not want configuration issues to surface at such a late point in time,
 * so we verify at application startup that at least one of a connection string or a namespace
 * is configured.
 * <p>
 * This check is only included in the deployment if the extension is enabled.
 */
public class ServiceBusConfigVerifier {

    @ConfigProperty(name = CONFIG_KEY_CONNECTION_STRING)
    Optional<String> connectionString;

    @ConfigProperty(name = CONFIG_KEY_NAMESPACE)
    Optional<String> namespace;

    @Startup
    void verifyCdiProducerConfiguration() {
        if (connectionString.isEmpty() && namespace.isEmpty()) {
            throw new ConfigurationException(String.format(
                    "Either the connection string (%s) or the namespace (%s) must be set.\n" +
                            "Alternatively, you can disable the CDI producers of the Azure Service Bus extension at build time with '%s=false'.",
                    CONFIG_KEY_CONNECTION_STRING, CONFIG_KEY_NAMESPACE, CONFIG_KEY_ENABLED));
        }
    }
}
