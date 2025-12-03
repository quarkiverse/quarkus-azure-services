package io.quarkiverse.azure.servicebus.deployment;

import static io.quarkiverse.azure.servicebus.deployment.ServiceBusDevServicesConfig.CONFIG_KEY_DEVSERVICES_ENABLED;
import static io.quarkiverse.azure.servicebus.deployment.ServiceBusDevServicesConfig.CONFIG_KEY_LICENSE_ACCEPTED;
import static io.quarkiverse.azure.servicebus.deployment.ServiceBusProcessor.FEATURE;
import static io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig.CONFIG_KEY_CONNECTION_STRING;
import static io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig.CONFIG_KEY_NAMESPACE;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.jboss.logging.Logger;
import org.testcontainers.utility.MountableFile;

import io.quarkiverse.azure.servicebus.deployment.ServiceBusDevServicesConfig.EmulatorConfig;
import io.quarkus.deployment.IsDevServicesSupportedByLaunchMode;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.*;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.devservices.common.ComposeLocator;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.configuration.ConfigUtils;
import io.quarkus.runtime.configuration.ConfigurationException;

public class ServiceBusDevServicesProcessor {

    private static final String SERVICEBUS_EULA_URL = "https://github.com/Azure/azure-service-bus-emulator-installer/blob/main/EMULATOR_EULA.txt";
    private static final String MSSQL_SERVER_EULA_URL = "https://go.microsoft.com/fwlink/?linkid=857698";
    private static final String CONNECTION_STRING_FORMAT = "Endpoint=sb://%s:%d;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;";

    private static final Logger log = Logger.getLogger(ServiceBusDevServicesProcessor.class);

    /**
     * Label to add to a shared Azure Service Bus emulator running in containers.
     * This allows other applications to discover the running service and use it instead of starting a new instance.
     */
    static final String DEV_SERVICE_LABEL = "quarkus-dev-service-azure-servicebus";
    static final int EMULATOR_PORT = 5672;

    private static final ContainerLocator emulatorContainerLocator = new ContainerLocator(DEV_SERVICE_LABEL, EMULATOR_PORT);

    @BuildStep(onlyIf = { IsDevServicesSupportedByLaunchMode.class, DevServicesConfig.Enabled.class,
            ServiceBusDevServicesConfig.Enabled.class })
    public DevServicesResultBuildItem startDevService(
            ServiceBusDevServicesConfig serviceBusDevServicesConfig,
            DevServicesConfig devServicesConfig,
            LaunchModeBuildItem launchMode,
            LiveReloadBuildItem liveReload,
            List<DevServicesSharedNetworkBuildItem> sharedNetwork,
            DevServicesComposeProjectBuildItem compose) {

        if (isServiceBusConnectionConfigured()) {
            return null;
        }
        ensureLicenceIsAccepted(serviceBusDevServicesConfig);

        boolean useSharedNetwork = DevServicesSharedNetworkBuildItem.isSharedNetworkRequired(devServicesConfig, sharedNetwork);

        return emulatorContainerLocator
                .locateContainer(serviceBusDevServicesConfig.serviceName(), serviceBusDevServicesConfig.shared(),
                        launchMode.getLaunchMode())
                .or(() -> ComposeLocator.locateContainer(compose,
                        List.of(serviceBusDevServicesConfig.emulator().imageName()),
                        EMULATOR_PORT, launchMode.getLaunchMode(), useSharedNetwork))
                .map(containerAddress -> DevServicesResultBuildItem.discovered()
                        .name(FEATURE)
                        .containerId(containerAddress.getId())
                        .config(Map.of(CONFIG_KEY_CONNECTION_STRING,
                                CONNECTION_STRING_FORMAT.formatted(containerAddress.getHost(), containerAddress.getPort())))
                        .build())
                .orElseGet(() -> {
                    MountableFile configFile = mountableConfigFile(serviceBusDevServicesConfig.emulator());
                    RelaunchControllingConfig serviceConfig = toRelaunchControllingConfig(
                            serviceBusDevServicesConfig.emulator(), configFile, liveReload);
                    return DevServicesResultBuildItem.owned()
                            .name(FEATURE)
                            .serviceName(serviceBusDevServicesConfig.serviceName())
                            .serviceConfig(serviceConfig)
                            .startable(
                                    () -> createContainers(serviceBusDevServicesConfig, configFile, useSharedNetwork,
                                            launchMode))
                            .postStartHook(logStarted())
                            .configProvider(Map.of(CONFIG_KEY_CONNECTION_STRING, ServiceBusDevService::getConnectionInfo))
                            .build();
                });
    }

    private static boolean isServiceBusConnectionConfigured() {
        return ConfigUtils.isAnyPropertyPresent(List.of(CONFIG_KEY_NAMESPACE, CONFIG_KEY_CONNECTION_STRING));
    }

    private static void ensureLicenceIsAccepted(ServiceBusDevServicesConfig devServicesConfig) throws ConfigurationException {
        if (!devServicesConfig.licenseAccepted()) {
            throw new ConfigurationException(String.format(
                    """
                            To use the Azure Service Bus Dev Services, you must accept the license terms of the Azure Service Bus emulator (%s) and the Microsoft SQL Server (%s).
                            Either accept the licenses by setting '%s=true' or disable the Azure Service Bus Dev Services with '%s=false'.
                            """,
                    SERVICEBUS_EULA_URL, MSSQL_SERVER_EULA_URL, CONFIG_KEY_LICENSE_ACCEPTED, CONFIG_KEY_DEVSERVICES_ENABLED));
        }
    }

    private static MountableFile mountableConfigFile(EmulatorConfig emulatorConfig) {
        try {
            var emulatorConfigResolver = new ServiceBusEmulatorConfigResolver(emulatorConfig.configFilePath());
            Optional<Path> configFile = emulatorConfigResolver.getConfigFile();

            if (configFile.isEmpty()) {
                logFallbackConfigurationUsage();
                return ServiceBusEmulatorConfigResolver.getFallbackConfiguration();
            } else {
                log.debugf("Using configuration file at %s", configFile.get());
                return MountableFile.forHostPath(configFile.get().toAbsolutePath());
            }
        } catch (FileNotFoundException e) {
            throw configurationExceptionForMissingConfigFile(emulatorConfig);
        }
    }

    private static void logFallbackConfigurationUsage() {
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
    }

    private static ConfigurationException configurationExceptionForMissingConfigFile(EmulatorConfig emulatorConfig) {
        return new ConfigurationException(String.format(
                """
                        The Azure Service Bus emulator configuration file was not found at the location specified with '%s'.
                        Either add a configuration file at '%s/%s' or disable the Azure Service Bus Dev Services with '%s=false'.
                        """,
                EmulatorConfig.CONFIG_KEY_CONFIG_FILE_PATH, EmulatorConfig.CONFIG_FILE_DIRECTORY,
                emulatorConfig.configFilePath().get(), CONFIG_KEY_DEVSERVICES_ENABLED));
    }

    private static RelaunchControllingConfig toRelaunchControllingConfig(EmulatorConfig emulatorConfig,
            MountableFile configFile, LiveReloadBuildItem liveReload) {
        Optional<LocalDateTime> timestamp = Optional.ofNullable(liveReload.getContextObject(LocalDateTime.class));
        if (timestamp.isEmpty() || configFileChanged(configFile, liveReload)) {
            // initialize or update the timestamp in the context object
            LocalDateTime now = LocalDateTime.now();
            liveReload.setContextObject(LocalDateTime.class, now);
            return new RelaunchControllingConfig(emulatorConfig, now);
        } else {
            // reuse the existing timestamp from the context object
            return new RelaunchControllingConfig(emulatorConfig, timestamp.get());
        }
    }

    private static boolean configFileChanged(MountableFile configFile, LiveReloadBuildItem liveReload) {
        return liveReload.getChangedResources().stream()
                .anyMatch(configFile.getResolvedPath()::equals);
    }

    private static ServiceBusDevService createContainers(ServiceBusDevServicesConfig serviceBusDevServicesConfig,
            MountableFile configFile, boolean useSharedNetwork, LaunchModeBuildItem launchMode) {
        return new ServiceBusDevService(serviceBusDevServicesConfig, configFile, useSharedNetwork, launchMode.getLaunchMode());
    }

    private Consumer<ServiceBusDevService> logStarted() {
        return serviceBusDevService -> log.infof("Azure Service Bus emulator started. Connection string: %s",
                serviceBusDevService.getConnectionInfo());
    }

    @BuildStep(onlyIf = { DevServicesConfig.Enabled.class, ServiceBusDevServicesConfig.Enabled.class, IsDevelopment.class })
    public HotDeploymentWatchedFileBuildItem watchEmulatorConfigFile(ServiceBusDevServicesConfig devServicesConfig) {
        try {
            return watchConfigFile(devServicesConfig.emulator());
        } catch (FileNotFoundException e) {
            // Configuration errors are treated at dev service startup,
            // so we can safely ignore them here.
            return null;
        }
    }

    private HotDeploymentWatchedFileBuildItem watchConfigFile(EmulatorConfig emulatorConfig) throws FileNotFoundException {
        var emulatorConfigResolver = new ServiceBusEmulatorConfigResolver(emulatorConfig.configFilePath());
        Optional<Path> configFile = emulatorConfigResolver.getConfigFile();

        if (configFile.isPresent()) {
            Path path = configFile.get();
            log.debugf("Watching %s for changes", path);
            return new HotDeploymentWatchedFileBuildItem(path.toAbsolutePath().toString());
        } else {
            return null;
        }
    }
}
