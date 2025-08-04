package io.quarkiverse.azure.servicebus.deployment;

import static io.quarkiverse.azure.servicebus.deployment.ServiceBusDevServicesConfig.CONFIG_KEY_DEVSERVICE_ENABLED;
import static io.quarkiverse.azure.servicebus.deployment.ServiceBusDevServicesConfig.CONFIG_KEY_LICENSE_ACCEPTED;
import static io.quarkiverse.azure.servicebus.deployment.ServiceBusProcessor.FEATURE;
import static io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig.CONFIG_KEY_CONNECTION_STRING;
import static io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig.CONFIG_KEY_NAMESPACE;

import java.nio.file.Path;
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
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem.RunningDevService;
import io.quarkus.deployment.builditem.DevServicesSharedNetworkBuildItem;
import io.quarkus.deployment.builditem.HotDeploymentWatchedFileBuildItem;
import io.quarkus.deployment.builditem.LiveReloadBuildItem;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.logging.Log;
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
    public List<DevServicesResultBuildItem> startDevServices(ServiceBusDevServicesConfig devServicesConfig,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem, LiveReloadBuildItem liveReload,
            BuildProducer<ValidationErrorBuildItem> configErrors) {

        if (isServiceBusConnectionConfigured() || hasConfigurationProblems(devServicesConfig, configErrors)) {
            return null;
        }

        if (liveReload.isLiveReload()) {
            Log.info("Live reload triggered - shutting down existing dev services");
            stopServiceBusEmulator();
        }

        if (devServices == null) {
            devServices = startServiceBusEmulator(devServicesConfig, devServicesSharedNetworkBuildItem);
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

    private void stopServiceBusEmulator() {
        if (devServices != null) {
            for (RunningDevService service : devServices) {
                try {
                    Log.debugf("Shutting down dev service: %s", service.getName());
                    service.close();
                    Log.debugf("Shut down dev service: %s", service.getName());
                } catch (Exception e) {
                    Log.warnf("Failed to shut down dev service %s: %s", service.getName(), e.getMessage());
                }
            }
            devServices = null;
        }
    }

    private List<RunningDevService> startServiceBusEmulator(ServiceBusDevServicesConfig devServicesConfig,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem) {
        log.info("Dev Services for Azure Service Bus starting the Azure Service Bus emulator");

        boolean useSharedNetwork = !devServicesSharedNetworkBuildItem.isEmpty();
        log.debug(useSharedNetwork ? "Using" : "Not using" + " a shared network");

        MountableFile configFile = configFile(devServicesConfig.emulator());
        log.debugf("Azure Service Bus emulator uses configuration file at '%s'", configFile.getResolvedPath());

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

        RunningDevService databaseDevService = new RunningDevService(FEATURE + " (database)", database.getContainerId(),
                database::close,
                Collections.emptyMap());
        RunningDevService emulatorDevService = new RunningDevService(FEATURE + " (emulator)", emulator.getContainerId(),
                emulator::close,
                configOverrides);
        return List.of(databaseDevService, emulatorDevService);
    }

    private MountableFile configFile(EmulatorConfig emulatorConfig) {
        Optional<Path> effectiveConfigFilePath = emulatorConfig.effectiveConfigFilePath();

        if (effectiveConfigFilePath.isPresent()) {
            // either configured location or default location.
            return MountableFile.forHostPath(effectiveConfigFilePath.get());
        } else {
            if (isConfigFilePathConfigured(emulatorConfig)) {
                throw configurationExceptionForMissingConfigFile(emulatorConfig);
            } else {
                return fallbackConfiguration();
            }
        }
    }

    private static boolean isConfigFilePathConfigured(EmulatorConfig emulatorConfig) {
        return emulatorConfig.configFilePath().isPresent();
    }

    private ConfigurationException configurationExceptionForMissingConfigFile(EmulatorConfig emulatorConfig) {
        return new ConfigurationException(String.format(
                """
                        The Azure Service Bus emulator configuration file was not found at the location specified with '%s'.
                        Either add a configuration file at '%s/%s' or disable the Service Bus Dev Services with '%s=false'.
                        """,
                EmulatorConfig.CONFIG_KEY_CONFIG_FILE_PATH, EmulatorConfig.CONFIG_FILE_DIRECTORY,
                emulatorConfig.configFilePath().get(), CONFIG_KEY_DEVSERVICE_ENABLED));
    }

    private MountableFile fallbackConfiguration() {
        log.warnf(
                """
                        To use the Dev Services for Azure Service Bus, a configuration file for the Azure Service Bus emulator must be provided.
                        Place it at '%s/%s'.
                        See %s for an example configuration file.
                        """,
                EmulatorConfig.CONFIG_FILE_DIRECTORY, EmulatorConfig.DEFAULT_CONFIG_FILE_NAME,
                EmulatorConfig.EXAMPLE_CONFIG_FILE_URL);
        log.warn(
                "Azure Service Bus emulator launching with a fallback configuration that provides a queue 'queue' and a topic 'topic' with a subscription 'subscription'.");
        return MountableFile.forClasspathResource(EmulatorConfig.FALLBACK_CONFIG_FILE_RESOURCE_PATH);
    }

    @BuildStep(onlyIf = { DevServicesConfig.Enabled.class, ServiceBusDevServicesConfig.Enabled.class, IsDevelopment.class })
    public HotDeploymentWatchedFileBuildItem watchEmulatorConfigFile(ServiceBusDevServicesConfig devServicesConfig) {
        Optional<Path> configFilePath = devServicesConfig.emulator().effectiveConfigFilePath();

        if (configFilePath.isPresent()) {
            Path path = configFilePath.get();
            log.debugf("Watching '%s' for changes", path);
            return new HotDeploymentWatchedFileBuildItem(path.toAbsolutePath().toString());
        } else {
            return null;
        }
    }
}
