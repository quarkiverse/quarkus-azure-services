package io.quarkiverse.azure.servicebus.deployment;

import static io.quarkiverse.azure.servicebus.deployment.ServiceBusProcessor.FEATURE;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.testcontainers.azure.ServiceBusEmulatorContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.MountableFile;

import io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem.RunningDevService;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;

public class ServiceBusDevServicesProcessor {

    static volatile List<RunningDevService> devServices;

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = { DevServicesConfig.Enabled.class,
            ServiceBusDevServicesConfig.Enabled.class })
    public List<DevServicesResultBuildItem> startServiceBusEmulator() {
        if (devServices == null) {
            devServices = startContainers();
        }
        return devServices.stream()
                .map(RunningDevService::toBuildItem)
                .toList();
    }

    private List<RunningDevService> startContainers() {
        Network internalNetwork = Network.newNetwork();

        MSSQLServerContainer<?> database = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-CU14-ubuntu-22.04")
                .acceptLicense()
                .withNetwork(internalNetwork);

        ServiceBusEmulatorContainer emulator = new ServiceBusEmulatorContainer(
                "mcr.microsoft.com/azure-messaging/servicebus-emulator:1.1.2")
                .acceptLicense()
                .withConfig(MountableFile.forClasspathResource("/servicebus-config.json"))
                .withMsSqlServerContainer(database)
                .withNetwork(internalNetwork);

        emulator.start();

        Map<String, String> configOverrides = Map.of(ServiceBusConfig.CONNECTION_STRING, emulator.getConnectionString());

        RunningDevService databaseDevService = new RunningDevService(FEATURE, database.getContainerId(), database::close,
                Collections.emptyMap());
        RunningDevService emulatorDevService = new RunningDevService(FEATURE, emulator.getContainerId(), emulator::close,
                configOverrides);
        return List.of(databaseDevService, emulatorDevService);
    }
}
