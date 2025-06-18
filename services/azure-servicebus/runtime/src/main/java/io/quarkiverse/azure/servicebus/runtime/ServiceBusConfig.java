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

    /**
     * The name of the property to enable or disable the extension.
     */
    String CONFIG_KEY_ENABLED = PREFIX + ".enabled";

    /**
     * The name of the property to configure the connection string.
     *
     * @see #connectionString()
     */
    String CONFIG_KEY_CONNECTION_STRING = PREFIX + ".connection-string";

    /**
     * The name of the property to configure the namespace.
     *
     * @see #namespace()
     */
    String CONFIG_KEY_NAMESPACE = PREFIX + ".namespace";

    /**
     * The name of the property to configure the domain name.
     *
     * @see #domainName()
     */
    String CONFIG_KEY_DOMAIN_NAME = PREFIX + ".domain-name";

    /**
     * The default value of {@link #domainName()}.
     */
    String DEFAULT_DOMAIN_NAME = "servicebus.windows.net";

    /**
     * Connect to the Service Bus using this connection string.
     * If set, authentication is handled by the SAS key in the connection string.
     * Otherwise, a DefaultAzureCredentialBuilder will be used for authentication,
     * and namespace and domain have to be configured.
     */
    Optional<String> connectionString();

    /**
     * The namespace of the Service Bus.
     * The domain name is appended to this value to form the fully qualified namespace name.
     *
     * @see #domainName()
     */
    Optional<String> namespace();

    /**
     * The domain name of the Service Bus.
     * The domain name is appended to the namespace to form the fully qualified namespace name.
     */
    @WithDefault(DEFAULT_DOMAIN_NAME)
    String domainName();
}
