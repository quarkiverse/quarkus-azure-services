package io.quarkiverse.azure.cosmos.deployment;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import io.quarkus.deployment.IsProduction;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesSharedNetworkBuildItem;
import io.quarkus.deployment.builditem.DockerStatusBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.builditem.Startable;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.configuration.ConfigUtils;

public class DevServicesCosmosProcessor {

    private static final Logger log = Logger.getLogger(DevServicesCosmosProcessor.class);
    private static final String DEV_SERVICE_LABEL = "quarkus-dev-service-azure-cosmos";
    static final String CONFIG_KEY_COSMOS_ENDPOINT = "quarkus.azure.cosmos.endpoint";
    static final String CONFIG_KEY_COSMOS_KEY = "quarkus.azure.cosmos.key";
    static final String CONFIG_KEY_DEFAULT_GATEWAY_MODE = "quarkus.azure.cosmos.default-gateway-mode";

    private static final ContainerLocator containerLocator = new ContainerLocator(DEV_SERVICE_LABEL,
            CosmosContainer.EXPOSED_PORT);

    @BuildStep(onlyIfNot = IsProduction.class, onlyIf = { DevServicesConfig.Enabled.class })
    public DevServicesResultBuildItem startCosmosDBContainer(
            LaunchModeBuildItem launchMode,
            DockerStatusBuildItem dockerStatusBuildItem,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            CosmosBuildTimeConfig buildTimeConfig,
            DevServicesConfig devServicesConfig) {

        CosmosDevServicesConfig cosmosDevServicesConfig = buildTimeConfig.devservices();
        if (!cosmosDevServicesConfig.enabled()) {
            log.info("Cosmos Dev Services is disabled");
            return null;
        }

        if (isCosmosConfigured()) {
            log.info("Cosmos Dev Services is not starting because the endpoint is configured");
            return null;
        }

        if (!ConfigUtils.getFirstOptionalValue(List.of("quarkus.azure.cosmos.enabled"), boolean.class)
                .orElse(true)) {
            log.info("Cosmos Dev Services is not starting because Cosmos config is explicitly disabled.");
            return null;
        }

        if (!dockerStatusBuildItem.isContainerRuntimeAvailable()) {
            log.warn(
                    "Please configure quarkus.azure.cosmos.endpoint for Azure Cosmos client or get a working docker instance");
            return null;
        }

        // The emulator uses a self-signed certificate. This SDK property must be set
        // before the application creates its Cosmos client.
        System.setProperty("COSMOS.EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED", "true");

        boolean useSharedNetwork = DevServicesSharedNetworkBuildItem.isSharedNetworkRequired(devServicesConfig,
                devServicesSharedNetworkBuildItem);
        return containerLocator
                .locateContainer(cosmosDevServicesConfig.serviceName(), cosmosDevServicesConfig.shared(),
                        launchMode.getLaunchMode())
                .map(containerAddress -> DevServicesResultBuildItem.discovered()
                        .feature(CosmosProcessor.FEATURE)
                        .containerId(containerAddress.getId())
                        .config(Map.of(CONFIG_KEY_COSMOS_ENDPOINT,
                                CosmosContainer.getEndpoint(containerAddress.getHost(), containerAddress.getPort()),
                                CONFIG_KEY_COSMOS_KEY, CosmosContainer.getKey(),
                                CONFIG_KEY_DEFAULT_GATEWAY_MODE, "true"))
                        .build())
                .orElseGet(() -> DevServicesResultBuildItem.owned()
                        .feature(CosmosProcessor.FEATURE)
                        .serviceName(cosmosDevServicesConfig.serviceName())
                        .serviceConfig(cosmosDevServicesConfig)
                        .startable(() -> new CosmosContainer(cosmosDevServicesConfig.serviceName(), useSharedNetwork,
                                devServicesConfig.timeout()))
                        .configProvider(Map.of(
                                CONFIG_KEY_COSMOS_ENDPOINT, CosmosContainer::getEndpoint,
                                CONFIG_KEY_COSMOS_KEY, container -> CosmosContainer.getKey(),
                                CONFIG_KEY_DEFAULT_GATEWAY_MODE, container -> "true"))
                        .build());
    }

    private boolean isCosmosConfigured() {
        return ConfigUtils.isPropertyPresent(CONFIG_KEY_COSMOS_ENDPOINT);
    }

    private static class CosmosContainer extends GenericContainer<CosmosContainer> implements Startable {

        private final boolean useSharedNetwork;
        private String hostName = null;
        static final int EXPOSED_PORT = 8081;

        CosmosContainer(String serviceName, boolean useSharedNetwork, Optional<Duration> timeout) {
            super("mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator:vnext-preview");
            addExposedPort(EXPOSED_PORT);
            waitingFor(Wait.forLogMessage("Now listening.*", 1));
            withLabel(DEV_SERVICE_LABEL, serviceName);
            withEnv(
                    Map.of(
                            "PROTOCOL", "https",
                            "PORT", "" + EXPOSED_PORT));
            this.useSharedNetwork = useSharedNetwork;
            if (timeout.isPresent()) {
                withStartupTimeout(timeout.get());
            }
        }

        @Override
        public void start() {
            super.start();
        }

        @Override
        public void close() {
            super.close();
        }

        @Override
        public String getConnectionInfo() {
            return getEndpoint();
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
            String host = getHost();
            // COSMOS.EMULATOR_HOST is needed so the Azure Cosmos SDK recognizes the dynamically mapped Testcontainers host as the Cosmos emulator.
            System.setProperty("COSMOS.EMULATOR_HOST", host);
            return getEndpoint(host, getPort());
        }

        @Override
        protected void configure() {
            super.configure();

            if (useSharedNetwork) {
                hostName = ConfigureUtil.configureSharedNetwork(this, CosmosProcessor.FEATURE);
            }
        }

        final int getPort() {
            return useSharedNetwork ? EXPOSED_PORT : super.getFirstMappedPort();
        }

        @Override
        public String getHost() {
            return useSharedNetwork ? hostName : super.getHost();
        }

        static String getEndpoint(String host, int port) {
            return "https://" + host + ":" + port;
        }

    }

}
