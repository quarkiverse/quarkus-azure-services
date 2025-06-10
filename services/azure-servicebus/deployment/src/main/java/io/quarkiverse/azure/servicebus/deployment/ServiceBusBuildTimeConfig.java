package io.quarkiverse.azure.servicebus.deployment;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigRoot
@ConfigMapping(prefix = "quarkus.azure.servicebus")
public interface ServiceBusBuildTimeConfig {

    /**
     * The flag to enable the extension.
     * If set to false, the CDI producers will be disabled.
     */
    @WithDefault("true")
    boolean enabled();

}
