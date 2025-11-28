package io.quarkiverse.azure.servicebus.deployment;

import static io.quarkiverse.azure.servicebus.deployment.ServiceBusDevServicesProcessor.DEV_SERVICE_LABEL;
import static io.quarkus.devservices.common.ConfigureUtil.configureLabels;

import org.testcontainers.azure.ServiceBusEmulatorContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.MountableFile;

import io.quarkus.deployment.builditem.Startable;
import io.quarkus.runtime.LaunchMode;

/**
 * Manages the lifecycle and configuration of Azure Service Bus emulator container
 * and its prerequisite Microsoft SQL Server database container.
 * <p>
 * This class deliberately does not extend {@link GenericContainer} as it manages multiple containers
 * rather than representing a single container instance.
 */
class ServiceBusDevService implements Startable {

    private final MSSQLServerContainer<?> database;
    private final ServiceBusEmulatorContainer emulator;

    public ServiceBusDevService(ServiceBusDevServicesConfig config, MountableFile configFile,
            boolean useSharedNetwork, LaunchMode launchMode) {
        this.database = new MSSQLServerContainer<>(config.database().imageName())
                .acceptLicense()
                .withNetwork(Network.SHARED)
                .withLabel(DEV_SERVICE_LABEL, config.serviceName());

        this.emulator = new ServiceBusEmulatorContainer(
                config.emulator().imageName()) {
            @Override
            public String getHost() {
                return useSharedNetwork ? getNetworkAliases().get(0) : super.getHost();
            }

            @Override
            public Integer getMappedPort(int originalPort) {
                return useSharedNetwork ? ServiceBusDevServicesProcessor.EMULATOR_PORT : super.getMappedPort(originalPort);
            }
        }
                .acceptLicense()
                .withConfig(configFile)
                .withMsSqlServerContainer(database)
                .withNetwork(Network.SHARED)
                .withLabel(DEV_SERVICE_LABEL, config.serviceName());

        configureLabels(database, launchMode);
        configureLabels(emulator, launchMode);
    }

    @Override
    public void start() {
        emulator.start(); // this implicitly starts the connected database container
    }

    @Override
    public String getConnectionInfo() {
        return emulator.getConnectionString();
    }

    @Override
    public String getContainerId() {
        return emulator.getContainerId();
    }

    @Override
    public void close() {
        emulator.close();
        database.close();
    }

}
