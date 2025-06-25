package io.quarkiverse.azure.servicebus.deployment;

import io.smallrye.config.WithDefault;

public interface ServiceBusDevServicesEmulatorConfig {
    String DEFAULT_IMAGE_NAME = "mcr.microsoft.com/azure-messaging/servicebus-emulator:latest";

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
     * <p>
     * The default image uses the {@code latest} tag.
     * This can lead to build failures if a new incompatible version is published.
     * For stability, it is recommended to specify an explicit version tag.
     * <p>
     * This extension has been tested and verified working with version
     * {@code mcr.microsoft.com/azure-messaging/servicebus-emulator:1.1.2}.
     */
    @WithDefault(DEFAULT_IMAGE_NAME)
    String imageName();
}
