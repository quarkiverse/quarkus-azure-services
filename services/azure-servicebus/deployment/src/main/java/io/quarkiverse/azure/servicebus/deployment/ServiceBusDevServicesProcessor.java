package io.quarkiverse.azure.servicebus.deployment;

import static io.quarkiverse.azure.servicebus.deployment.ServiceBusProcessor.FEATURE;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.testcontainers.azure.ServiceBusEmulatorContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.MountableFile;

import io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig;
import io.quarkus.arc.deployment.ValidationPhaseBuildItem.ValidationErrorBuildItem;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem.RunningDevService;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.runtime.configuration.ConfigUtils;
import io.quarkus.runtime.configuration.ConfigurationException;

public class ServiceBusDevServicesProcessor {

    private static final String EMULATOR_CONFIG_FILE = "servicebus-config.json";
    static volatile List<RunningDevService> devServices;

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = { DevServicesConfig.Enabled.class,
            ServiceBusDevServicesConfig.Enabled.class })
    public List<DevServicesResultBuildItem> startServiceBusEmulator(BuildProducer<ValidationErrorBuildItem> configErrors) {
        if (isServiceBusConnectionConfigured() || hasConfigurationProblems(configErrors)) {
            return null;
        }

        if (devServices == null) {
            devServices = startContainers();
        }

        return devServices.stream()
                .map(RunningDevService::toBuildItem)
                .toList();
    }

    private static boolean isServiceBusConnectionConfigured() {
        return ConfigUtils.isPropertyPresent(ServiceBusConfig.CONFIG_KEY_NAMESPACE)
                || ConfigUtils.isPropertyPresent(ServiceBusConfig.CONFIG_KEY_CONNECTION_STRING);
    }

    private static boolean hasConfigurationProblems(BuildProducer<ValidationErrorBuildItem> configErrors) {
        if (isEmulatorConfigFileMissing()) {
            configErrors.produce(new ValidationErrorBuildItem(new ConfigurationException(
                    "The Service Bus emulator configuration file was not found at 'src/main/resources/%s'."
                            .formatted(EMULATOR_CONFIG_FILE))));
            return true;
        }
        return false;
    }

    private static boolean isEmulatorConfigFileMissing() {
        URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(EMULATOR_CONFIG_FILE);
        return resourceUrl == null;
    }

    private List<RunningDevService> startContainers() {
        Network internalNetwork = Network.newNetwork();

        MSSQLServerContainer<?> database = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-CU14-ubuntu-22.04")
                .acceptLicense()
                .withNetwork(internalNetwork);

        ServiceBusEmulatorContainer emulator = new ServiceBusEmulatorContainer(
                "mcr.microsoft.com/azure-messaging/servicebus-emulator:1.1.2")
                .acceptLicense()
                .withConfig(MountableFile.forClasspathResource(EMULATOR_CONFIG_FILE))
                .withMsSqlServerContainer(database)
                .withNetwork(internalNetwork);

        emulator.start();

        Map<String, String> configOverrides = Map.of(ServiceBusConfig.CONFIG_KEY_CONNECTION_STRING,
                emulator.getConnectionString());

        RunningDevService databaseDevService = new RunningDevService(FEATURE, database.getContainerId(), database::close,
                Collections.emptyMap());
        RunningDevService emulatorDevService = new RunningDevService(FEATURE, emulator.getContainerId(), emulator::close,
                configOverrides);
        return List.of(databaseDevService, emulatorDevService);
    }
}
