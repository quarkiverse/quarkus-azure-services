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
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.*;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
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

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = { DevServicesConfig.Enabled.class })
    public DevServicesResultBuildItem startKeyVaultContainer(
            LaunchModeBuildItem launchMode,
            DockerStatusBuildItem dockerStatusBuildItem,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            KeyVaultBuildTimeConfig buildTimeConfig,
            DevServicesConfig devServicesConfig) {
        return startContainer(dockerStatusBuildItem, buildTimeConfig.devservices(), launchMode.getLaunchMode(),
                devServicesConfig.timeout());
    }

    private DevServicesResultBuildItem startContainer(
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
                    "Please configure quarkus.azure.keyvault.secret.endpoint for Azure Key Vault client or get a working container instance");
            return null;
        }

        return DevServicesResultBuildItem.owned()
                .feature(KeyVaultSecretProcessor.FEATURE)
                .serviceName(DEV_SERVICE_NAME)
                .serviceConfig(keyVaultDevServicesConfig)
                .startable(() -> new KeyVaultDevService(keyVaultDevServicesConfig, timeout))
                .configProvider(service -> configMap(keyVaultDevServicesConfig, service.container))
                .postStartHook(service -> log.infof("The Key Vault container %s is ready to accept connections",
                        service.getContainerId()))
                .build();
    }

    private class KeyVaultDevService implements Startable {
        private final LowkeyVaultContainer container;
        private final KeyVaultDevServicesConfig config;

        private KeyVaultDevService(KeyVaultDevServicesConfig config, Optional<Duration> timeout) {
            this.config = config;
            int tokenPort = config.managedIdentity()
                    .map(KeyVaultDevServicesManagedIdentityConfig::tokenPort)
                    .orElseGet(KeyVaultDevServicesProcessor.this::findFreePort);
            LowkeyVaultContainerBuilder builder = lowkeyVault(config.imageName())
                    .hostTokenPort(tokenPort);
            if (config.mergeSslKeystoreWithApplicationKeystore()) {
                builder.mergeTrustStores();
            }
            container = builder
                    .build()
                    .withStartupTimeout(timeout.orElse(Duration.ofSeconds(15)))
                    .withLabel(DEV_SERVICE_LABEL, DEV_SERVICE_NAME);
        }

        @Override
        public void start() {
            container.start();
            if (!config.preSetSecrets().isEmpty()) {
                SecretClient secretClient = container.getClientFactory().getSecretClientBuilderForDefaultVault().buildClient();
                config.preSetSecrets().forEach((name, value) -> {
                    log.infof("Pre-setting secret '%s'", name);
                    secretClient.setSecret(name, value);
                });
            }
        }

        @Override
        public String getConnectionInfo() {
            return container.getEndpointBaseUrl();
        }

        @Override
        public String getContainerId() {
            return container.getContainerId();
        }

        @Override
        public void close() {
            container.close();
        }
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
