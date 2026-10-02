package io.quarkiverse.azure.keyvault.secret.deployment;

import static com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainerBuilder.lowkeyVault;

import java.net.ServerSocket;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.jboss.logging.Logger;

import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainer;
import com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainerBuilder;

import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DockerStatusBuildItem;
import io.quarkus.deployment.builditem.Startable;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
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
            DockerStatusBuildItem dockerStatusBuildItem,
            KeyVaultBuildTimeConfig buildTimeConfig,
            DevServicesConfig devServicesConfig) {

        KeyVaultDevServicesConfig keyVaultDevServicesConfig = buildTimeConfig.devservices();
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

        Map<String, Function<KeyVaultDevService, String>> configProvider = new HashMap<>();
        configProvider.put(CONFIG_KEY_KEYVAULT_ENDPOINT, KeyVaultDevService::getEndpoint);
        configProvider.put(CONFIG_KEY_KEYVAULT_DISABLE_CRV, service -> "true");
        if (keyVaultDevServicesConfig.managedIdentity().isEmpty()) {
            configProvider.put(CONFIG_KEY_KEYVAULT_USERNAME, KeyVaultDevService::getUsername);
            configProvider.put(CONFIG_KEY_KEYVAULT_PASSWORD, KeyVaultDevService::getPassword);
        }

        return DevServicesResultBuildItem.owned()
                .feature(KeyVaultSecretProcessor.FEATURE)
                .serviceName(DEV_SERVICE_NAME)
                .serviceConfig(keyVaultDevServicesConfig)
                .startable(() -> new KeyVaultDevService(keyVaultDevServicesConfig, devServicesConfig.timeout()))
                .configProvider(configProvider)
                .postStartHook(service -> log.infof("The Key Vault container %s is ready to accept connections",
                        service.getContainerId()))
                .build();
    }

    private static class KeyVaultDevService implements Startable {
        private final KeyVaultDevServicesConfig config;
        private final LowkeyVaultContainer container;

        private KeyVaultDevService(KeyVaultDevServicesConfig config, Optional<Duration> timeout) {
            this.config = config;
            int tokenPort = config.managedIdentity()
                    .map(KeyVaultDevServicesManagedIdentityConfig::tokenPort)
                    .orElseGet(KeyVaultDevService::findFreePort);
            LowkeyVaultContainerBuilder builder = lowkeyVault(config.imageName())
                    .hostTokenPort(tokenPort);
            if (config.mergeSslKeystoreWithApplicationKeystore()) {
                builder.mergeTrustStores();
            }
            this.container = builder
                    .build()
                    .withStartupTimeout(timeout.orElse(Duration.ofSeconds(15)))
                    .withLabel(DEV_SERVICE_LABEL, DEV_SERVICE_NAME);
        }

        @Override
        public void start() {
            container.start();
            if (!config.preSetSecrets().isEmpty()) {
                SecretClient secretClient = new SecretClientBuilder()
                        .vaultUrl(getEndpoint())
                        .credential(new BasicAuthenticationCredential(container.getUsername(), container.getPassword()))
                        .disableChallengeResourceVerification()
                        .buildClient();
                config.preSetSecrets().forEach((name, value) -> {
                    log.infof("Pre-setting secret '%s'", name);
                    secretClient.setSecret(name, value);
                });
            }
        }

        @Override
        public String getConnectionInfo() {
            return getEndpoint();
        }

        @Override
        public String getContainerId() {
            return container.getContainerId();
        }

        @Override
        public void close() {
            container.close();
        }

        private String getEndpoint() {
            return container.getEndpointBaseUrl().replace(container.getHost(), "localhost");
        }

        private String getUsername() {
            return container.getUsername();
        }

        private String getPassword() {
            return container.getPassword();
        }

        private static int findFreePort() {
            try (ServerSocket socket = new ServerSocket(0)) {
                return socket.getLocalPort();
            } catch (Exception e) {
                throw new RuntimeException("Unable to find free port", e);
            }
        }
    }

    private boolean isKeyVaultConfigured() {
        return ConfigUtils.isPropertyPresent(CONFIG_KEY_KEYVAULT_ENDPOINT);
    }
}
