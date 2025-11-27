package io.quarkiverse.azure.servicebus.deployment;

import static io.quarkiverse.azure.servicebus.deployment.ServiceBusDevServicesConfig.PREFIX;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigRoot
@ConfigMapping(prefix = PREFIX)
public interface ServiceBusDevServicesConfig {
    String PREFIX = "quarkus.azure.servicebus.devservices";

    /**
     * The name of the property to enable or disable the Dev Services.
     */
    String CONFIG_KEY_DEVSERVICES_ENABLED = PREFIX + ".enabled";

    /**
     * The name of the property to accept the EULA of Azure Service Bus emulator and MSSQL Server.
     */
    String CONFIG_KEY_LICENSE_ACCEPTED = PREFIX + ".license-accepted";

    /**
     * The name of the property to configure whether the Dev Services should be shared or not.
     */
    String CONFIG_KEY_SHARED = PREFIX + ".shared";

    /**
     * The name of the property to set a custom service name for the Dev Services.
     */
    String CONFIG_KEY_SERVICE_NAME = PREFIX + ".service-name";

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
     * To use the Azure Service Bus Dev Services, you must accept the license terms
     * of the Azure Service Bus emulator and the Microsoft SQL Server.
     */
    @WithDefault("false")
    boolean licenseAccepted();

    /**
     * Indicates if the Azure Service Bus emulator managed by Quarkus Dev Services is shared.
     * When shared, Quarkus looks for running containers using label-based service discovery.
     * If a matching container is found, it is used, and so a second one is not started.
     * Otherwise, Dev Services for Azure Service Bus starts a new container.
     * <p>
     * The discovery uses the {@code quarkus-dev-service-azure-servicebus} label.
     * The value is configured using the {@code service-name} property.
     * <p>
     * Container sharing is only used in dev mode.
     */
    @WithDefault("true")
    boolean shared();

    /**
     * The value of the {@code quarkus-dev-service-azure-servicebus} label attached to the started container.
     * This property is used when {@code shared} is set to {@code true}.
     * In this case, before starting a container, Dev Services for Azure Service Bus looks for a container with the
     * {@code quarkus-dev-service-azure-servicebus} label set to the configured value.
     * If found, it will use this container instead of starting a new one.
     * Otherwise, it starts a new container with the {@code quarkus-dev-service-azure-servicebus} label
     * set to the specified value.
     * <p>
     * This property is used when you need multiple shared Azure Service Bus emulator instances.
     */
    @WithDefault("azure-servicebus-emulator")
    String serviceName();

    /**
     * Configuration of the Azure Service Bus emulator.
     */
    EmulatorConfig emulator();

    /**
     * Configuration of the Microsoft SQL Server required by the Azure Service Bus emulator.
     */
    DatabaseConfig database();

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

    /**
     * Configuration of the Azure Service Bus emulator.
     */
    interface EmulatorConfig {
        String DEFAULT_IMAGE_NAME = "mcr.microsoft.com/azure-messaging/servicebus-emulator:latest";

        /**
         * The directory for storing Azure Service Bus emulator configuration files, relative to the project root.
         */
        String CONFIG_FILE_DIRECTORY = "src/main/azure/servicebus-emulator";

        /**
         * Name of the default configuration file in {@value #CONFIG_FILE_DIRECTORY}.
         */
        String DEFAULT_CONFIG_FILE_NAME = "config.json";

        String EXAMPLE_CONFIG_FILE_URL = "https://github.com/Azure/azure-service-bus-emulator-installer/blob/main/ServiceBus-Emulator/Config/Config.json";

        /**
         * Prefix for the properties defined in this interface.
         */
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
         * The name of the property to configure a custom Azure Service Bus emulator configuration file.
         */
        String CONFIG_KEY_CONFIG_FILE_PATH = PREFIX + ".config-file-path";

        /**
         * Name and path of the Azure Service Bus emulator configuration file relative to the directory
         * {@code src/main/azure/servicebus-emulator}, e.g. {@code my-servicebus-config.json}.
         * <p>
         * If you need custom configuration for different test scenarios,
         * this property allows you to specify distinct configuration files for each test profile.
         * <p>
         * If a configuration file is specified with this property, it must exist.
         * If the property is not used, the path defaults to {@code config.json}.
         * If it does not exist there, a warning is issued and the emulator will use a fallback configuration file
         * that provides a queue "queue" and a topic "topic" with a subscription "subscription".
         * <p>
         * <a href=
         * "https://github.com/Azure/azure-service-bus-emulator-installer/blob/main/ServiceBus-Emulator/Config/Config.json">Example
         * configuration file</a>
         */
        Optional<String> configFilePath();
    }

    /**
     * Configuration of the Microsoft SQL Server required by the Azure Service Bus emulator.
     */
    interface DatabaseConfig {
        String DEFAULT_IMAGE_NAME = "mcr.microsoft.com/mssql/server:latest";

        /**
         * Prefix for the properties defined in this interface.
         */
        String PREFIX = ServiceBusDevServicesConfig.PREFIX + ".database";
        /**
         * The name of the property to configure the container image name of the
         * Microsoft SQL Server required by the Azure Service Bus emulator.
         */
        String CONFIG_KEY_IMAGE_NAME = PREFIX + ".image-name";

        /**
         * The container image name of the Microsoft SQL Server required by the Azure Service Bus emulator.
         * See the <a href="https://mcr.microsoft.com/en-us/artifact/mar/mssql/server/tags">artifact
         * registry</a> for available tags of the default image.
         * <p>
         * The default image uses the {@code latest} tag.
         * This can lead to build failures if a new incompatible version is published.
         * For stability, it is recommended to specify an explicit version tag.
         * <p>
         * This extension has been tested and verified working with version
         * {@code mcr.microsoft.com/mssql/server:2022-CU14-ubuntu-22.04}.
         */
        @WithDefault(DEFAULT_IMAGE_NAME)
        String imageName();
    }
}
