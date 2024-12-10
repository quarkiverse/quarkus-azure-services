package io.quarkiverse.azure.eventhubs.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.azure.eventhubs")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface EventhubsConfig {

    /**
     * The flag to enable the eventhubs. If set to false, the eventhubs will be disabled
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * The fully qualified namespace of Azure Eventhubs. Required if quarkus.azure.eventhubs.enabled is set to true
     */
    String fullyQualifiedNamespace();

    /**
     * The name of the event hub. Required if quarkus.azure.eventhubs.enabled is set to true
     */
    String eventHubName();
}
