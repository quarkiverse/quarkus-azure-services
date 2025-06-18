package io.quarkiverse.azure.servicebus.deployment;

import io.smallrye.config.WithDefault;

public interface ServiceBusDevServicesEmulatorConfig {
    String DEFAULT_IMAGE_NAME = "mcr.microsoft.com/azure-messaging/servicebus-emulator:1.1.2";

    String PREFIX = ServiceBusDevServicesConfig.PREFIX + ".emulator";
    /**
     * The name of the property to configure the container image name of the
     * Azure Service Bus emulator.
     */
    String CONFIG_KEY_IMAGE_NAME = PREFIX + ".image-name";

    /**
     * The container image name of the Azure Service Bus emulator.
     * See the <a href="https://mcr.microsoft.com/en-us/artifact/mar/azure-messaging/servicebus-emulator/tags">artifact
     * registry</a> for available tags of the default image.
     */
    @WithDefault(DEFAULT_IMAGE_NAME)
    String imageName();
}
