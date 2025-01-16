package io.quarkiverse.azure.app.configuration;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.azure.app.configuration")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface AzureAppConfigurationConfig {

    /** The flag to enable the app configuration. If set to false, the app configuration will be disabled */
    @WithDefault("true")
    boolean enabled();

    /** The endpoint of the app configuration. Required if quarkus.azure.app.configuration.enabled is set to true */
    Optional<String> endpoint();

    /**
     * The id of the app configuration. Required if quarkus.azure.app.configuration.enabled is set to true and access keys are
     * used for authentication
     */
    Optional<String> id();

    /**
     * The secret of the app configuration. Required if quarkus.azure.app.configuration.enabled is set to true and access keys
     * are used for authentication
     */
    Optional<String> secret();

    /** The label filter of the app configuration. Use comma as separator for multiple label names */
    Optional<String> labels();

    /** The connection string */
    default String connectionString() {
        if (!enabled()) {
            return "";
        }
        assert endpoint().isPresent() : "The endpoint of the app configuration must be set";

        if (id().isEmpty() || secret().isEmpty()) {
            return "";
        }
        return "Endpoint=" + endpoint().get() + ";Id=" + id().get() + ";Secret=" + secret().get();
    }
}
