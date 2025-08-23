package io.quarkiverse.azure.keyvault.secret.deployment;

import static com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainerBuilder.lowkeyVault;

import java.net.ServerSocket;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jboss.logging.Logger;
import org.jetbrains.annotations.NotNull;

import com.azure.security.keyvault.secrets.SecretClient;
import com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainer;
import com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainerBuilder;

import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.*;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem.RunningDevService;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.configuration.ConfigUtils;

public class KeyVaultDevServicesProcessor {

    private static final Logger log = Logger.getLogger(KeyVaultDevServicesProcessor.class);
    private static final String DEV_SERVICE_LABEL = "quarkus-dev-service-azure-keyvault";
    private static final String DEV_SERVICE_NAME = "lowkey-vault";
    static final String CONFIG_KEY_KEYVAULT_ENDPOINT = "quarkus.azure.keyvault.secret.endpoint";
    static final String CONFIG_KEY_KEYVAULT_DISABLE_CRV = "quarkus.azure.keyvault.secret.local-configuration.disable-challenge-resource-verification";
    static final String CONFIG_KEY_KEYVAULT_USERNAME = "quarkus.azure.keyvault.secret.local-configuration.basic-authentication.username";
    static final String CONFIG_KEY_KEYVAULT_PASSWORD = "quarkus.azure.keyvault.secret.local-configuration.basic-authentication.password";
    private static volatile KeyVaultDevServicesConfig capturedDevServicesConfiguration;
    private static volatile boolean first = true;
    private static volatile RunningDevService devService;

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = { DevServicesConfig.Enabled.class })
    public void startKeyVaultContainer(BuildProducer<DevServicesResultBuildItem> devConfig,
            LaunchModeBuildItem launchMode,
            DockerStatusBuildItem dockerStatusBuildItem,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            KeyVaultBuildTimeConfig buildTimeConfig,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            CuratedApplicationShutdownBuildItem closeBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem,
            DevServicesConfig devServicesConfig) {

        KeyVaultDevServicesConfig keyVaultDevServicesConfig = buildTimeConfig.devservices();

        // Figure out if we need to shut down and restart the existing Lowkey Vault
        // container. If not and the container has already started, we just return.
        if (devService != null) {
            if (!isRestartRequired(keyVaultDevServicesConfig)) {
                return;
            }
            stopRunningDevService();
        }

        capturedDevServicesConfiguration = keyVaultDevServicesConfig;

        StartupLogCompressor compressor = new StartupLogCompressor(
                (launchMode.isTest() ? "(test) " : "") + "Azure Key Vault Dev Services Starting:",
                consoleInstalledBuildItem,
                loggingSetupBuildItem);
        try {
            devService = startContainer(
                    dockerStatusBuildItem,
                    keyVaultDevServicesConfig,
                    launchMode.getLaunchMode(),
                    devServicesConfig.timeout());
            if (devService != null) {
                devConfig.produce(devService.toBuildItem());
                log.infof("The Key Vault container %s is ready to accept connections",
                        devService.getContainerId());
            }
            compressor.close();
        } catch (Throwable t) {
            compressor.closeAndDumpCaptured();
            throw new RuntimeException(t);
        }

        addGracefulShutdown(closeBuildItem);
    }

    private static void addGracefulShutdown(CuratedApplicationShutdownBuildItem closeBuildItem) {
        if (first) {
            first = false;
            Runnable closeTask = () -> {
                if (devService != null) {
                    try {
                        devService.close();
                    } catch (Throwable t) {
                        log.error("Failed to stop Key Vault container", t);
                    }
                    devService = null;
                }
                first = true;
            };
            closeBuildItem.addCloseTask(closeTask, true);
        }
    }

    private static void stopRunningDevService() {
        try {
            devService.close();
        } catch (Throwable e) {
            log.error("Failed to stop Key Vault container", e);
        }
        devService = null;
        capturedDevServicesConfiguration = null;
    }

    private static boolean isRestartRequired(KeyVaultDevServicesConfig keyVaultDevServicesConfig) {
        return !keyVaultDevServicesConfig.equals(capturedDevServicesConfiguration);
    }

    private RunningDevService startContainer(
            DockerStatusBuildItem dockerStatusBuildItem,
            KeyVaultDevServicesConfig keyVaultDevServicesConfig,
            LaunchMode launchMode,
            Optional<Duration> timeout) {
        if (launchMode == LaunchMode.NORMAL) {
            log.info("Key Vault Dev Services is disabled in normal mode");
            return null;
        }
        if (!keyVaultDevServicesConfig.enabled()) {
            log.info("Key Vault Dev Services is disabled");
            return null;
        }

        if (isKeyVaultConfigured()) {
            log.info("Key Vault Dev Services is not starting because the endpoint is configured");
            return null;
        }

        if (!ConfigUtils.getFirstOptionalValue(List.of("quarkus.azure.keyvault.secret.enabled"), boolean.class)
                .orElse(true)) {
            log.info("Key Vault Dev Services is not starting because Key Vault config is explicitly disabled.");
            return null;
        }

        if (!dockerStatusBuildItem.isContainerRuntimeAvailable()) {
            log.warn(
                    "Please configure quarkus.azure.keyvault.secret.endpoint for Azure Key Vault client or get a working docker instance");
            return null;
        }

        int tokenPort = keyVaultDevServicesConfig.managedIdentity()
                .map(KeyVaultDevServicesManagedIdentityConfig::tokenPort)
                .orElseGet(this::findFreePort);
        LowkeyVaultContainerBuilder builder = lowkeyVault(keyVaultDevServicesConfig.imageName())
                .hostTokenPort(tokenPort);
        if (keyVaultDevServicesConfig.mergeSslKeystoreWithApplicationKeystore()) {
            builder.mergeTrustStores();
        }
        LowkeyVaultContainer container = builder
                .build()
                .withStartupTimeout(timeout.orElse(Duration.ofSeconds(15)))
                .withLabel(DEV_SERVICE_LABEL, DEV_SERVICE_NAME);
        container.start();
        if (!keyVaultDevServicesConfig.preSetSecrets().isEmpty()) {
            SecretClient secretClient = container.getClientFactory().getSecretClientBuilderForDefaultVault().buildClient();
            keyVaultDevServicesConfig.preSetSecrets().forEach((name, value) -> {
                log.infof("Pre-setting secret '%s'", name);
                secretClient.setSecret(name, value);
            });
        }
        return new RunningDevService(
                KeyVaultSecretProcessor.FEATURE,
                container.getContainerId(),
                container::close,
                configMap(keyVaultDevServicesConfig, container));

    }

    private static @NotNull Map<String, String> configMap(
            KeyVaultDevServicesConfig keyVaultDevServicesConfig,
            LowkeyVaultContainer container) {
        Map<String, String> config = new HashMap<>();
        config.put(CONFIG_KEY_KEYVAULT_ENDPOINT, container.getEndpointBaseUrl());
        config.put(CONFIG_KEY_KEYVAULT_DISABLE_CRV, "true");
        if (keyVaultDevServicesConfig.managedIdentity().isEmpty()) {
            config.put(CONFIG_KEY_KEYVAULT_USERNAME, container.getUsername());
            config.put(CONFIG_KEY_KEYVAULT_PASSWORD, container.getPassword());
        }
        return config;
    }

    private boolean isKeyVaultConfigured() {
        return ConfigUtils.isPropertyPresent(CONFIG_KEY_KEYVAULT_ENDPOINT);
    }

    private int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (Exception e) {
            throw new RuntimeException("Unable to find free port", e);
        }
    }
}
