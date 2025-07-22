package io.quarkiverse.azure.servicebus.deployment;

import static io.quarkiverse.azure.servicebus.deployment.ServiceBusDevServicesConfig.CONFIG_KEY_DEVSERVICE_ENABLED;
import static io.quarkiverse.azure.servicebus.deployment.ServiceBusDevServicesConfig.CONFIG_KEY_LICENSE_ACCEPTED;
import static io.quarkiverse.azure.servicebus.deployment.ServiceBusDevServicesConfig.EmulatorConfig.CONFIG_FILE_DIRECTORY;
import static io.quarkiverse.azure.servicebus.deployment.ServiceBusProcessor.FEATURE;
import static io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig.CONFIG_KEY_CONNECTION_STRING;
import static io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig.CONFIG_KEY_NAMESPACE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jboss.logging.Logger;
import org.testcontainers.azure.ServiceBusEmulatorContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.MountableFile;

import io.quarkiverse.azure.servicebus.deployment.ServiceBusDevServicesConfig.EmulatorConfig;
import io.quarkus.arc.deployment.ValidationPhaseBuildItem.ValidationErrorBuildItem;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem.RunningDevService;
import io.quarkus.deployment.builditem.DevServicesSharedNetworkBuildItem;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.runtime.configuration.ConfigUtils;
import io.quarkus.runtime.configuration.ConfigurationException;

public class ServiceBusDevServicesProcessor {

    private static final Logger log = Logger.getLogger(ServiceBusDevServicesProcessor.class);
    private static final int DEFAULT_EMULATOR_PORT = 5672;
    public static final String SERVICEBUS_EULA_URL = "https://github.com/Azure/azure-service-bus-emulator-installer/blob/main/EMULATOR_EULA.txt";
    public static final String MSSQL_SERVER_EULA_URL = "https://go.microsoft.com/fwlink/?linkid=857698";
    static volatile List<RunningDevService> devServices;

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = { DevServicesConfig.Enabled.class,
            ServiceBusDevServicesConfig.Enabled.class })
    public List<DevServicesResultBuildItem> startServiceBusEmulator(ServiceBusDevServicesConfig devServicesConfig,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            BuildProducer<ValidationErrorBuildItem> configErrors) {
        if (isServiceBusConnectionConfigured() || hasConfigurationProblems(devServicesConfig, configErrors)) {
            return null;
        }

        if (devServices == null) {
            devServices = startContainers(devServicesConfig, devServicesSharedNetworkBuildItem);
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
                    """
                            To use the Service Bus Dev Services, you must accept the license terms of the Service Bus emulator (%s) and the Microsoft SQL Server (%s).
                            Either accept the licenses by setting '%s=true' or disable the Service Bus Dev Services with '%s=false'.
                            """,
                    SERVICEBUS_EULA_URL, MSSQL_SERVER_EULA_URL, CONFIG_KEY_LICENSE_ACCEPTED, CONFIG_KEY_DEVSERVICE_ENABLED))));
            return true;
        }

        return false;
    }

    private List<RunningDevService> startContainers(ServiceBusDevServicesConfig devServicesConfig,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem) {
        log.info("Dev Services for Azure Service Bus starting the Azure Service Bus emulator");

        MountableFile configFile = configFile(devServicesConfig);
        log.debugf("Azure Service Bus emulator launching with configuration file at '%s'", configFile.getResolvedPath());

        boolean useSharedNetwork = !devServicesSharedNetworkBuildItem.isEmpty();

        MSSQLServerContainer<?> database = new MSSQLServerContainer<>(devServicesConfig.database().imageName())
                .acceptLicense()
                .withNetwork(Network.SHARED);

        ServiceBusEmulatorContainer emulator = new ServiceBusEmulatorContainer(
                devServicesConfig.emulator().imageName()) {
            @Override
            public String getHost() {
                return useSharedNetwork ? getNetworkAliases().get(0) : super.getHost();
            }

            @Override
            public Integer getMappedPort(int originalPort) {
                return useSharedNetwork ? DEFAULT_EMULATOR_PORT : super.getMappedPort(originalPort);
            }
        }
                .acceptLicense()
                .withConfig(configFile)
                .withMsSqlServerContainer(database)
                .withNetwork(Network.SHARED);

        emulator.start();
        log.infof("Azure Service Bus emulator started - connection string is '%s'", emulator.getConnectionString());

        Map<String, String> configOverrides = Map.of(
                CONFIG_KEY_CONNECTION_STRING, emulator.getConnectionString());

        RunningDevService databaseDevService = new RunningDevService(FEATURE, database.getContainerId(), database::close,
                Collections.emptyMap());
        RunningDevService emulatorDevService = new RunningDevService(FEATURE, emulator.getContainerId(), emulator::close,
                configOverrides);
        return List.of(databaseDevService, emulatorDevService);
    }

    private MountableFile configFile(ServiceBusDevServicesConfig devServicesConfig) {
        Optional<String> explicitConfigFilePath = devServicesConfig.emulator().configFilePath();

        if (explicitConfigFilePath.isPresent()) {
            Path configFilePath = Paths.get(CONFIG_FILE_DIRECTORY, explicitConfigFilePath.get());
            if (Files.exists(configFilePath)) {
                return MountableFile.forHostPath(configFilePath);
            } else {
                throw new ConfigurationException(String.format(
                        """
                                The Azure Service Bus emulator configuration file was not found at the location specified with '%s'.
                                Either add a configuration file at '%s' or disable the Service Bus Dev Services with '%s=false'.
                                """,
                        EmulatorConfig.CONFIG_KEY_CONFIG_FILE_PATH, configFilePath, CONFIG_KEY_DEVSERVICE_ENABLED));
            }
        }

        Path defaultConfigFilePath = Paths.get(CONFIG_FILE_DIRECTORY, EmulatorConfig.DEFAULT_CONFIG_FILE_NAME);
        if (Files.exists(defaultConfigFilePath)) {
            return MountableFile.forHostPath(defaultConfigFilePath);
        }

        log.warnf(
                """
                        To use the Dev Services for Azure Service Bus, a configuration file for the Azure Service Bus emulator must be provided.
                        Place it at '%s/%s'.
                        See %s for an example configuration file.
                        """,
                EmulatorConfig.CONFIG_FILE_DIRECTORY, EmulatorConfig.DEFAULT_CONFIG_FILE_NAME,
                EmulatorConfig.EXAMPLE_CONFIG_FILE_URL);

        log.warn(
                "Azure Service Bus emulator using a fallback configuration that provides a queue 'queue' and a topic 'topic' with a subscription 'subscription'.");

        return MountableFile.forClasspathResource(EmulatorConfig.FALLBACK_CONFIG_FILE_PATH);
    }
}
