package io.quarkiverse.azure.servicebus.runtime;

import static io.quarkus.runtime.annotations.ConfigPhase.BUILD_AND_RUN_TIME_FIXED;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.azure.servicebus")
@ConfigRoot(phase = BUILD_AND_RUN_TIME_FIXED)
public interface ServiceBusBuildTimeConfig {

    /**
     * The flag to enable the extension.
     * If set to false, the CDI producers will be disabled.
     */
    @WithDefault("true")
    boolean enabled();

}
