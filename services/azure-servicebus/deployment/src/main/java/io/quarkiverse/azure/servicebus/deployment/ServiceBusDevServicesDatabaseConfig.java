package io.quarkiverse.azure.servicebus.deployment;

import io.smallrye.config.WithDefault;

public interface ServiceBusDevServicesDatabaseConfig {
    String DEFAULT_IMAGE_NAME = "mcr.microsoft.com/mssql/server:latest";

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
