package io.quarkiverse.azure.servicebus.deployment;

import static io.quarkiverse.azure.servicebus.deployment.ServiceBusDevServicesConfig.PREFIX;

import java.util.function.BooleanSupplier;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigRoot
@ConfigMapping(prefix = PREFIX)
public interface ServiceBusDevServicesConfig {
    String PREFIX = "quarkus.azure.servicebus.devservices";

    /**
     * The name of the property to enable or disable the DevServices.
     */
    String CONFIG_KEY_DEVSERVICE_ENABLED = PREFIX + ".enabled";

    /**
     * The name of the property to accept the EULA of Azure Service Bus emulator and MSSQL Server.
     */
    String CONFIG_KEY_LICENCE_ACCEPTED = PREFIX + ".licence-accepted";

    /**
     * Whether Dev Services should be enabled or not.
     * Dev Services are enabled by default unless a specific Azure Service Bus connection exists.
     * <p>
     * When Dev Services are enabled, Quarkus will attempt to automatically configure and start
     * an Azure Service Bus emulator instance and an associated MSSQL Server when running in dev
     * or test mode and when Docker is running.
     * {@code quarkus.azure.servicebus.connection-string} will be set to point to the emulator.
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * To use the Service Bus Dev Services, you must accept the license terms of the Service Bus emulator and the Microsoft SQL
     * Server.
     */
    @WithDefault("false")
    boolean licenceAccepted();

    class Enabled implements BooleanSupplier {
        final ServiceBusDevServicesConfig config;

        public Enabled(ServiceBusDevServicesConfig config) {
            this.config = config;
        }

        @Override
        public boolean getAsBoolean() {
            return config.enabled();
        }
    }

}
