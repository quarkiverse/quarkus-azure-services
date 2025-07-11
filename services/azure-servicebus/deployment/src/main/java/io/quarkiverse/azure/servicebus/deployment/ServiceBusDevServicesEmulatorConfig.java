package io.quarkiverse.azure.servicebus.deployment;

import java.util.Optional;

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

    /**
     * The name of the property to configure a custom Service Bus emulator configuration file location.
     */
    String CONFIG_KEY_CONFIG_FILE_PATH = PREFIX + ".config-file-path";

    /**
     * Name and path of the Service Bus emulator configuration file.
     * The value is interpreted as a relative path in the classpath, e.g. {@code my-servicebus-config.json},
     * that points to a valid Service Bus emulator configuration file in JSON format.
     * <p>
     * If you need custom configuration for different test scenarios,
     * this property allows you to specify distinct configuration files for each test profile.
     * <p>
     * If a configuration file is specified with this property, it must exist.
     * If the property is not used, the configuration is expected to reside at {@code servicebus-config.json}.
     * If it does not exist there, a warning is issued and the emulator will use a default configuration file
     * that defines a queue "queue" and a topic "topic" with a subscription "subscription".
     * <p>
     * <a href=
     * "https://github.com/Azure/azure-service-bus-emulator-installer/blob/main/ServiceBus-Emulator/Config/Config.json">Example
     * configuration file</a>
     */
    Optional<String> configFilePath();
}
