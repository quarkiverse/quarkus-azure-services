package io.quarkiverse.azure.cosmos.deployment;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigRoot
@ConfigMapping(prefix = "quarkus.azure.cosmos")
public interface CosmosBuildTimeConfig {

    /**
     * Dev Services configuration.
     */
    CosmosDevServicesConfig devservices();
}
