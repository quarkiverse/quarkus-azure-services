package io.quarkiverse.azure.storage.blob.deployment;

import static io.quarkus.runtime.LaunchMode.DEVELOPMENT;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesSharedNetworkBuildItem;
import io.quarkus.deployment.builditem.DockerStatusBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.builditem.Startable;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.configuration.ConfigUtils;

public class DevServicesStorageBlobProcessor {

    static final String IMAGE = "mcr.microsoft.com/azure-storage/azurite:3.35.0";
    private static final Logger log = Logger.getLogger(DevServicesStorageBlobProcessor.class);
    private static final int EXPOSED_PORT = 10000;
    private static final String PROTOCOL = "http";
    private static final String ACCOUNT_NAME = "devstoreaccount1";
    private static final String ACCOUNT_KEY = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";
    private static final String CONFIG_KEY_CONNECTION_STRING = "quarkus.azure.storage.blob.connection-string";
    private static final String CONFIG_KEY_ENDPOINT = "quarkus.azure.storage.blob.endpoint";

    /**
     * Label to add to shared Dev Services for Azurite storage blob service running in containers.
     * This allows other applications to discover the running service and use it instead of starting a new instance.
     */
    private static final String DEV_SERVICE_LABEL = "quarkus-dev-service-azure-storage-blob";
    private static final ContainerLocator containerLocator = new ContainerLocator(DEV_SERVICE_LABEL, EXPOSED_PORT);

    public static String getBlobEndpoint(String host, int port) {
        return String.format("%s://%s:%s/%s", PROTOCOL, host, port, ACCOUNT_NAME);
    }

    public static String getConnectionString(String host, int port) {
        String blobEndpoint = getBlobEndpoint(host, port);
        return String.format("DefaultEndpointsProtocol=%s;AccountName=%s;AccountKey=%s;BlobEndpoint=%s;",
                PROTOCOL, ACCOUNT_NAME, ACCOUNT_KEY, blobEndpoint);
    }

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = { DevServicesConfig.Enabled.class })
    public DevServicesResultBuildItem startStorageBlobContainer(
            LaunchModeBuildItem launchMode,
            DockerStatusBuildItem dockerStatusBuildItem,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            StorageBlobBuildTimeConfig config,
            DevServicesConfig devServicesConfig) {
        return startContainer(dockerStatusBuildItem, config.devservices(), launchMode.getLaunchMode(),
                DevServicesSharedNetworkBuildItem.isSharedNetworkRequired(devServicesConfig,
                        devServicesSharedNetworkBuildItem),
                devServicesConfig.timeout());
    }

    private DevServicesResultBuildItem startContainer(DockerStatusBuildItem dockerStatusBuildItem,
            StorageBlobDevServicesConfig storageBlobDevServicesConfig, LaunchMode launchMode,
            boolean useSharedNetwork, Optional<Duration> timeout) {
        if (!storageBlobDevServicesConfig.enabled()) {
            // explicitly disabled
            log.info("Not starting devservice for Azure storage blob client as it has been disabled in the config");
            return null;
        }

        boolean needToStart = !ConfigUtils.isPropertyPresent(CONFIG_KEY_CONNECTION_STRING)
                && !ConfigUtils.isPropertyPresent(CONFIG_KEY_ENDPOINT);
        if (!needToStart) {
            log.info("Not starting devservice for Azure storage blob client as host has been provided");
            return null;
        }

        if (!dockerStatusBuildItem.isContainerRuntimeAvailable()) {
            log.warn(
                    "Please configure quarkus.azure.storage.blob.connection-string for Azure storage blob client or get a working docker instance");
            return null;
        }

        DockerImageName dockerImageName = DockerImageName.parse(storageBlobDevServicesConfig.imageName().orElse(IMAGE))
                .asCompatibleSubstituteFor(IMAGE);

        return containerLocator
                .locateContainer(storageBlobDevServicesConfig.serviceName(), storageBlobDevServicesConfig.shared(), launchMode)
                .map(containerAddress -> {
                    return DevServicesResultBuildItem.discovered()
                            .feature(StorageBlobProcessor.FEATURE)
                            .containerId(containerAddress.getId())
                            .config(Map.of(CONFIG_KEY_CONNECTION_STRING,
                                    getConnectionString(containerAddress.getHost(), containerAddress.getPort())))
                            .build();
                })
                .orElseGet(() -> DevServicesResultBuildItem.owned()
                        .feature(StorageBlobProcessor.FEATURE)
                        .serviceName(storageBlobDevServicesConfig.serviceName())
                        .serviceConfig(storageBlobDevServicesConfig)
                        .startable(() -> {
                            QuarkusPortAzuriteContainer container = new QuarkusPortAzuriteContainer(dockerImageName,
                                    storageBlobDevServicesConfig.port(),
                                    launchMode == DEVELOPMENT ? storageBlobDevServicesConfig.serviceName() : null,
                                    useSharedNetwork, storageBlobDevServicesConfig.skipApiVersionCheck());
                            timeout.ifPresent(container::withStartupTimeout);
                            return container;
                        })
                        .configProvider(container -> Map.of(CONFIG_KEY_CONNECTION_STRING, container.getConnectionInfo()))
                        .postStartHook(
                                container -> log.infof("The Azurite storage blob container %s is ready to accept connections",
                                        container.getContainerId()))
                        .build());
    }

    private static class QuarkusPortAzuriteContainer extends GenericContainer<QuarkusPortAzuriteContainer>
            implements Startable {
        private final OptionalInt fixedExposedPort;
        private final boolean useSharedNetwork;
        private final boolean skipApiVersionCheck;

        private String hostName = null;

        public QuarkusPortAzuriteContainer(DockerImageName dockerImageName, OptionalInt fixedExposedPort, String serviceName,
                boolean useSharedNetwork, boolean skipApiVersionCheck) {
            super(dockerImageName);
            this.fixedExposedPort = fixedExposedPort;
            this.useSharedNetwork = useSharedNetwork;
            this.skipApiVersionCheck = skipApiVersionCheck;

            if (serviceName != null) {
                withLabel(DEV_SERVICE_LABEL, serviceName);
            }
        }

        @Override
        protected void configure() {
            super.configure();

            if (useSharedNetwork) {
                hostName = ConfigureUtil.configureSharedNetwork(this, StorageBlobProcessor.FEATURE);
            } else {
                if (fixedExposedPort.isPresent()) {
                    addFixedExposedPort(fixedExposedPort.getAsInt(), EXPOSED_PORT);
                } else {
                    addExposedPort(EXPOSED_PORT);
                }
            }

            if (skipApiVersionCheck) {
                setCommand("azurite", "-l", "/data", "--blobHost", "0.0.0.0", "--queueHost", "0.0.0.0", "--tableHost",
                        "0.0.0.0",
                        "--skipApiVersionCheck");
            }
        }

        public int getPort() {
            if (useSharedNetwork) {
                return EXPOSED_PORT;
            }

            if (fixedExposedPort.isPresent()) {
                return fixedExposedPort.getAsInt();
            }
            return super.getFirstMappedPort();
        }

        @Override
        public String getConnectionInfo() {
            return getConnectionString(getHost(), getPort());
        }

        @Override
        public void close() {
            stop();
        }

        @Override
        public String getHost() {
            return useSharedNetwork ? hostName : super.getHost();
        }
    }
}
