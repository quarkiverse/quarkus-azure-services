package io.quarkiverse.azure.servicebus.runtime;

import static io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig.CONFIG_KEY_CONNECTION_STRING;
import static io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig.CONFIG_KEY_ENABLED;
import static io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig.CONFIG_KEY_NAMESPACE;

import java.util.Optional;

import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;

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
@Priority(Interceptor.Priority.APPLICATION + 100)
public class ServiceBusConfigVerifier {

    /**
     * The priority for this verifier to run.
     * We want it to run very early to catch configuration issues as soon as possible,
     * but still after framework initialization to ensure all required components are ready.
     */
    private static final int PRIORITY = Interceptor.Priority.LIBRARY_BEFORE;

    @ConfigProperty(name = CONFIG_KEY_CONNECTION_STRING)
    Optional<String> connectionString;

    @ConfigProperty(name = CONFIG_KEY_NAMESPACE)
    Optional<String> namespace;

    @Startup(PRIORITY)
    void verifyCdiProducerConfiguration() {
        if (connectionString.isEmpty() && namespace.isEmpty()) {
            throw missingConnectionConfigurationException();
        }
    }

    /**
     * @return a {@link ConfigurationException} indicating no Azure Service Bus connection configuration was provided.
     */
    static ConfigurationException missingConnectionConfigurationException() {
        return new ConfigurationException(String.format(
                "Either the connection string (%s) or the namespace (%s) must be set.\n" +
                        "Alternatively, you can disable the CDI producers of the Azure Service Bus extension at build time with '%s=false'.",
                CONFIG_KEY_CONNECTION_STRING, CONFIG_KEY_NAMESPACE, CONFIG_KEY_ENABLED));
    }
}
