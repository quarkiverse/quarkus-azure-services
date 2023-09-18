package io.quarkiverse.azure.app.configuration;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "quarkus.azure.app.configuration")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface AzureAppConfigurationConfig {
    /** The endpoint of the app configuration */
    String endpoint();

    /** The id of the app configuration */
    String id();

    /** The secret of the app configuration */
    String secret();

    /** The label filter of the app configuration. Use comma as separator for multiple label names */
    Optional<String> labels();

    /** The connection string */
    default String connectionString() {
        return "Endpoint=" + endpoint() + ";Id=" + id() + ";Secret=" + secret();
    }
}
