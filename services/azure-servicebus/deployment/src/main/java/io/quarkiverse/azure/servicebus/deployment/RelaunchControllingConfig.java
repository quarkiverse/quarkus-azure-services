package io.quarkiverse.azure.servicebus.deployment;

import java.time.LocalDateTime;

import io.quarkus.deployment.builditem.LiveReloadBuildItem;

/**
 * Container for items that cause the dev services to relaunch when changed.
 * <p>
 * The dev services use a config object to determine whether to relaunch the container or not.
 * The {@link ServiceBusDevServicesConfig.EmulatorConfig} is not sufficient for us because we also want
 * to relaunch the container if the configuration file changes.
 * This information is represented as a {@link LocalDateTime} of the last seen modification of the config file
 * as reported by the Dev Services framework.
 * We pass this information between invocations via the {@link LiveReloadBuildItem} context object.
 *
 * @param emulatorConfig the user-supplied emulator configuration
 * @param configFileTimestamp the timestamp when the last modification of the config file was reported
 *        by the Dev Services framework
 */
record RelaunchControllingConfig(ServiceBusDevServicesConfig.EmulatorConfig emulatorConfig,
        LocalDateTime configFileTimestamp) {
}
