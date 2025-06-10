package io.quarkiverse.azure.servicebus.deployment;

import java.util.function.BooleanSupplier;

import io.smallrye.config.WithDefault;

public interface ServiceBusDevServicesConfig {

    /**
     * If the Dev Services have been explicitly enabled or disabled.
     * Dev Services are generally enabled by default.
     * <p>
     * When Dev Services are enabled, Quarkus will attempt to automatically configure and start
     * an Azure Service Bus emulator instance when running in Dev or Test mode and when Docker is running.
     * {@code quarkus.azure.servicebus.connection-string} will be set to point to the emulator.
     */
    @WithDefault("true")
    boolean enabled();

    class Enabled implements BooleanSupplier {
        final ServiceBusBuildTimeConfig config;

        public Enabled(ServiceBusBuildTimeConfig config) {
            this.config = config;
        }

        @Override
        public boolean getAsBoolean() {
            return config.devservices().enabled();
        }
    }

}
