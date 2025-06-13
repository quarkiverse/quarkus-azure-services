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
     * <p>
     * This flag does not affect Dev Services.
     * To disable Dev Services, you must explicitly set either
     * {@code quarkus.azure.servicebus.devservices.enabled}
     * or {@code quarkus.devservices.enabled} to {@code false}.
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * Dev Services configuration.
     */
    ServiceBusDevServicesConfig devservices();

}
