package io.quarkiverse.azure.cosmos.deployment;

import java.net.ServerSocket;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CuratedApplicationShutdownBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem.RunningDevService;
import io.quarkus.deployment.builditem.DevServicesSharedNetworkBuildItem;
import io.quarkus.deployment.builditem.DockerStatusBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.configuration.ConfigUtils;

public class DevServicesCosmosProcessor {

    private static final Logger log = Logger.getLogger(DevServicesCosmosProcessor.class);
    private static final String DEV_SERVICE_LABEL = "quarkus-dev-service-azure-cosmos";
    static final String CONFIG_KEY_COSMOS_ENDPOINT = "quarkus.azure.cosmos.endpoint";
    static final String CONFIG_KEY_COSMOS_KEY = "quarkus.azure.cosmos.key";
    private static volatile CosmosDevServicesConfig capturedDevServicesConfiguration;
    private static volatile boolean first = true;
    private static volatile RunningDevService devService;

    private static final ContainerLocator containerLocator = new ContainerLocator(DEV_SERVICE_LABEL,
            CosmosContainer.EXPOSED_PORT);

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = { DevServicesConfig.Enabled.class })
    public void startCosmosDBContainer(BuildProducer<DevServicesResultBuildItem> devConfig,
            LaunchModeBuildItem launchMode,
            DockerStatusBuildItem dockerStatusBuildItem,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            CosmosBuildTimeConfig buildTimeConfig,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            CuratedApplicationShutdownBuildItem closeBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem,
            DevServicesConfig devServicesConfig) {

        CosmosDevServicesConfig cosmosDevServicesConfig = buildTimeConfig.devservices();

        // figure out if we need to shut down and restart existing Cosmos
        // container
        // if not and the Cosmos container has already started we just
        // return
        if (devService != null) {
            boolean restartRequired = !cosmosDevServicesConfig.equals(capturedDevServicesConfiguration);
            if (!restartRequired) {
                return;
            }
            try {
                devService.close();
            } catch (Throwable e) {
                log.error("Failed to stop Cosmos container", e);
            }
            devService = null;
            capturedDevServicesConfiguration = null;
        }

        capturedDevServicesConfiguration = cosmosDevServicesConfig;

        StartupLogCompressor compressor = new StartupLogCompressor(
                (launchMode.isTest() ? "(test) " : "") + "Azure Cosmos Dev Services Starting:",
                consoleInstalledBuildItem,
                loggingSetupBuildItem);
        try {
            devService = startContainer(
                    dockerStatusBuildItem,
                    cosmosDevServicesConfig,
                    launchMode.getLaunchMode(),
                    !devServicesSharedNetworkBuildItem.isEmpty(),
                    devServicesConfig.timeout());
            if (devService != null) {
                devConfig.produce(devService.toBuildItem());
                log.infof("The Cosmos container %s is ready to accept connections",
                        devService.getContainerId());
            }
            compressor.close();
        } catch (Throwable t) {
            compressor.closeAndDumpCaptured();
            throw new RuntimeException(t);
        }

        if (first) {
            first = false;
            Runnable closeTask = () -> {
                if (devService != null) {
                    try {
                        devService.close();
                    } catch (Throwable t) {
                        log.error("Failed to stop Cosmos container", t);
                    }
                    devService = null;
                }
                first = true;
            };
            closeBuildItem.addCloseTask(closeTask, true);
        }
    }

    private RunningDevService startContainer(DockerStatusBuildItem dockerStatusBuildItem,
            CosmosDevServicesConfig cosmosDevServicesConfig,
            LaunchMode launchMode,
            boolean useSharedNetwork,
            Optional<Duration> timeout) {
        if (!cosmosDevServicesConfig.enabled()) {
            log.info("Cosmos Dev Services is disabled");
            return null;
        }

        if (isCosmosConfigured()) {
            log.info("Cosmos Dev Services is not starting because the endpoint and key are configured");
            return null;
        }

        if (!dockerStatusBuildItem.isContainerRuntimeAvailable()) {
            log.warn(
                    "Please configure quarkus.azure.cosmos.endpoint for Azure Cosmos client or get a working docker instance");
            return null;
        }

        // At this point, we know we need to use the emulator, so set the property that allows the client to
        // connect w/o SSL cert validation when used with the emulator
        System.setProperty("COSMOS.EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED", "true");

        Supplier<RunningDevService> cosmosServerSupplier = () -> {
            CosmosContainer container = new CosmosContainer(cosmosDevServicesConfig.serviceName(), useSharedNetwork,
                    timeout);
            container.start();
            return new RunningDevService(
                    CosmosProcessor.FEATURE,
                    container.getContainerId(),
                    container::close,
                    Map.of(CONFIG_KEY_COSMOS_ENDPOINT, container.getEndpoint(),
                            CONFIG_KEY_COSMOS_KEY, CosmosContainer.getKey()));

        };
        return containerLocator
                .locateContainer(cosmosDevServicesConfig.serviceName(), cosmosDevServicesConfig.shared(), launchMode)
                .map(containerAddress -> {
                    String endpoint = CosmosContainer.getEndpoint(containerAddress.getHost(), containerAddress.getPort());
                    return new RunningDevService(
                            CosmosProcessor.FEATURE,
                            containerAddress.getId(),
                            null,
                            Map.of(CONFIG_KEY_COSMOS_ENDPOINT, endpoint,
                                    CONFIG_KEY_COSMOS_KEY, CosmosContainer.getKey()));
                })
                .orElseGet(cosmosServerSupplier);

    }

    private boolean isCosmosConfigured() {
        return ConfigUtils.isPropertyPresent(CONFIG_KEY_COSMOS_ENDPOINT);
    }

    private static class CosmosContainer extends GenericContainer<CosmosContainer> {

        private final boolean useSharedNetwork;
        private String hostName = null;
        static final int EXPOSED_PORT = 8081;

        private final int safePort;

        CosmosContainer(String serviceName, boolean useSharedNetwork, Optional<Duration> timeout) {
            super("mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator:vnext-preview");
            safePort = findFreePort();
            addFixedExposedPort(getPort(), EXPOSED_PORT);
            setPortBindings(List.of(getPort() + ":" + getPort()));
            waitingFor(Wait.forLogMessage("Now listening.*", 1));
            withLabel(DEV_SERVICE_LABEL, serviceName);
            withEnv(
                    Map.of(
                            "PROTOCOL", "https",
                            "PORT", "" + getPort()));
            this.useSharedNetwork = useSharedNetwork;
            if (timeout.isPresent()) {
                withStartupTimeout(timeout.get());
            }
        }

        /**
         * Emulator key is a known constant and specified in Azure Cosmos DB Documents.
         * This key is also used as password for emulator certificate file.
         *
         * @return predefined emulator key
         * @see <a href=
         *      "https://docs.microsoft.com/en-us/azure/cosmos-db/local-emulator?tabs=ssl-netstd21#authenticate-requests">Azure
         *      Cosmos DB Documents</a>
         */
        static String getKey() {
            return "C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==";
        }

        /**
         * @return secure https emulator endpoint to send requests
         */
        String getEndpoint() {
            return getEndpoint(getHost(), getPort());
        }

        @Override
        protected void configure() {
            super.configure();

            if (useSharedNetwork) {
                hostName = ConfigureUtil.configureSharedNetwork(this, CosmosProcessor.FEATURE);
            }
        }

        final int getPort() {
            return safePort;
        }

        @Override
        public String getHost() {
            return useSharedNetwork ? hostName : super.getHost();
        }

        static String getEndpoint(String host, int port) {
            return "https://" + host + ":" + port;
        }

        static final int findFreePort() {
            try (ServerSocket socket = new ServerSocket(0)) {
                return socket.getLocalPort();
            } catch (Exception e) {
                throw new RuntimeException("Unable to find free port", e);
            }
        }
    }

}