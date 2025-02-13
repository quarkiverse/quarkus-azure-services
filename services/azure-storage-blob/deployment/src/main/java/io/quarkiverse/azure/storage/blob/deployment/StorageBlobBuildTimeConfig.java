package io.quarkiverse.azure.storage.blob.deployment;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigRoot
@ConfigMapping(prefix = "quarkus.azure.storage.blob")
public interface StorageBlobBuildTimeConfig {

    /**
     * Whether a health check is published in case the smallrye-health extension is present.
     */
    @WithName("health.enabled")
    @WithDefault("true")
    boolean healthEnabled();

    /**
     * Dev Services configuration.
     */
    StorageBlobDevServicesConfig devservices();
}
