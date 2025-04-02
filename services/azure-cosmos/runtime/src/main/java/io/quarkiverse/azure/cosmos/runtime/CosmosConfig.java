package io.quarkiverse.azure.cosmos.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.azure.cosmos")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface CosmosConfig {

    /**
     * The flag to enable the cosmos. If set to false, the cosmos will be disabled
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * The endpoint of Azure Cosmos DB. Required if quarkus.azure.cosmos.enabled is set to true
     */
    Optional<String> endpoint();

    /**
     * Whether to use the default GATEWAY mode. If set to true, the default gateway mode will be used.
     */
    @WithDefault("false")
    boolean defaultGatewayMode();

    /**
     * The key of Azure Cosmos DB. Optional and can be empty if the Azure Identity is used to authenticate
     */
    Optional<String> key();
}
