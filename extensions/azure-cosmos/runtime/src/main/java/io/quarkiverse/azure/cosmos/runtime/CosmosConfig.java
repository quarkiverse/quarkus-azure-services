package io.quarkiverse.azure.cosmos.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "azure.cosmos", phase = ConfigPhase.RUN_TIME)
public class CosmosConfig {

    /**
     * The flag to enable the cosmos. If set to false, the cosmos will be disabled
     */
    @ConfigItem(defaultValue = "true")
    public boolean enabled;

    /**
     * The endpoint of Azure Cosmos DB. Required if quarkus.azure.cosmos.enabled is set to true
     */
    @ConfigItem
    public Optional<String> endpoint;
}
