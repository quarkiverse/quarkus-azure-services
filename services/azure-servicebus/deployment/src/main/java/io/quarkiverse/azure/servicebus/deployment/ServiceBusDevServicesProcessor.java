package io.quarkiverse.azure.servicebus.deployment;

import static io.quarkiverse.azure.servicebus.deployment.ServiceBusDevServicesConfig.CONFIG_KEY_DEVSERVICE_ENABLED;
import static io.quarkiverse.azure.servicebus.deployment.ServiceBusDevServicesConfig.CONFIG_KEY_LICENSE_ACCEPTED;
import static io.quarkiverse.azure.servicebus.deployment.ServiceBusProcessor.FEATURE;
import static io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig.CONFIG_KEY_CONNECTION_STRING;
import static io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig.CONFIG_KEY_NAMESPACE;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.testcontainers.azure.ServiceBusEmulatorContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.MountableFile;

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
    public static final String SERVICEBUS_EULA_URL = "https://github.com/Azure/azure-service-bus-emulator-installer/blob/main/EMULATOR_EULA.txt";
    public static final String MSSQL_SERVER_EULA_URL = "https://hub.docker.com/r/microsoft/mssql-server";
    static volatile List<RunningDevService> devServices;

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = { DevServicesConfig.Enabled.class,
            ServiceBusDevServicesConfig.Enabled.class })
    public List<DevServicesResultBuildItem> startServiceBusEmulator(ServiceBusDevServicesConfig devServicesConfig,
            BuildProducer<ValidationErrorBuildItem> configErrors) {
        if (isServiceBusConnectionConfigured() || hasConfigurationProblems(devServicesConfig, configErrors)) {
            return null;
        }

        if (devServices == null) {
            devServices = startContainers(devServicesConfig);
        }

        return devServices.stream()
                .map(RunningDevService::toBuildItem)
                .toList();
    }

    private static boolean isServiceBusConnectionConfigured() {
        return ConfigUtils.isAnyPropertyPresent(List.of(CONFIG_KEY_NAMESPACE, CONFIG_KEY_CONNECTION_STRING));
    }

    private static boolean hasConfigurationProblems(ServiceBusDevServicesConfig devServicesConfig,
            BuildProducer<ValidationErrorBuildItem> configErrors) {
        if (!devServicesConfig.licenseAccepted()) {
            configErrors.produce(new ValidationErrorBuildItem(new ConfigurationException(String.format(
                    "To use the Service Bus Dev Services, you must accept the license terms of the Service Bus emulator (%s)"
                            + " and the Microsoft SQL Server (described at %s).\n" +
                            "Either accept the licenses by setting '%s=true' or disable the Service Bus Dev Services with '%s=false'.",
                    SERVICEBUS_EULA_URL, MSSQL_SERVER_EULA_URL, CONFIG_KEY_LICENSE_ACCEPTED, CONFIG_KEY_DEVSERVICE_ENABLED))));
            return true;
        }
        if (isEmulatorConfigFileMissing()) {
            configErrors.produce(new ValidationErrorBuildItem(new ConfigurationException(String.format(
                    "The Service Bus emulator configuration file was not found at 'src/main/resources/%s'.\n" +
                            "Either add it or disable the Service Bus Dev Services with '%s=false'.",
                    EMULATOR_CONFIG_FILE, CONFIG_KEY_DEVSERVICE_ENABLED))));
            return true;
        }
        return false;
    }

    private static boolean isEmulatorConfigFileMissing() {
        URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(EMULATOR_CONFIG_FILE);
        return resourceUrl == null;
    }

    private List<RunningDevService> startContainers(ServiceBusDevServicesConfig devServicesConfig) {
        Network internalNetwork = Network.newNetwork();

        MSSQLServerContainer<?> database = new MSSQLServerContainer<>(devServicesConfig.database().imageName())
                .acceptLicense()
                .withNetwork(internalNetwork);

        ServiceBusEmulatorContainer emulator = new ServiceBusEmulatorContainer(devServicesConfig.emulator().imageName())
                .acceptLicense()
                .withConfig(MountableFile.forClasspathResource(EMULATOR_CONFIG_FILE))
                .withMsSqlServerContainer(database)
                .withNetwork(internalNetwork);

        emulator.start();

        Map<String, String> configOverrides = Map.of(CONFIG_KEY_CONNECTION_STRING,
                emulator.getConnectionString());

        RunningDevService databaseDevService = new RunningDevService(FEATURE, database.getContainerId(), database::close,
                Collections.emptyMap());
        RunningDevService emulatorDevService = new RunningDevService(FEATURE, emulator.getContainerId(), emulator::close,
                configOverrides);
        return List.of(databaseDevService, emulatorDevService);
    }
}
