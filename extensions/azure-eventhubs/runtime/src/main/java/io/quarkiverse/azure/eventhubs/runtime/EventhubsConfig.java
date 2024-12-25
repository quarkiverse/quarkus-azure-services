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
     * The namespace of the event hub. Required if quarkus.azure.eventhubs.enabled is set to true
     */
    String namespace();

    /**
     * The domain name of the event hub. Required if quarkus.azure.eventhubs.enabled is set to true
     */
    @WithDefault("servicebus.windows.net")
    String domainName();

    /**
     * The name of the event hub. Required if quarkus.azure.eventhubs.enabled is set to true
     */
    String eventhubName();
}
