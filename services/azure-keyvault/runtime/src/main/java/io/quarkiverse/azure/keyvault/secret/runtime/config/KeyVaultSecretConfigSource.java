package io.quarkiverse.azure.keyvault.secret.runtime.config;

import static io.quarkiverse.azure.keyvault.secret.runtime.KeyVaultSecretClientBuilderUtil.configureClientBuilder;
import static io.quarkiverse.azure.keyvault.secret.runtime.config.KeyVaultSecretConfigUtil.getSecretReference;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.core.http.HttpClient;
import com.azure.core.http.vertx.VertxHttpClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

import io.quarkiverse.azure.core.util.AzureQuarkusIdentifier;
import io.smallrye.config.common.AbstractConfigSource;
import io.vertx.core.Vertx;

class KeyVaultSecretConfigSource extends AbstractConfigSource {
    /**
     * The ordinal is set to < 100 (which is the default) so that this config source is retrieved from last.
     */
    private static final int KEYVAULT_SECRET_ORDINAL = 50;

    private static final String CONFIG_SOURCE_NAME = "io.quarkiverse.azure.keyvault.secret.runtime.config";
    private static final Logger log = LoggerFactory.getLogger(KeyVaultSecretConfigSource.class);

    private final KeyVaultSecretConfig kvConfig;

    public KeyVaultSecretConfigSource(final KeyVaultSecretConfig kvConfig) {
        super(CONFIG_SOURCE_NAME, KEYVAULT_SECRET_ORDINAL);
        this.kvConfig = kvConfig;
    }

    private SecretClient createClient(String hostAuthority, Vertx vertx) {
        log.info("Creating Key Vault Secret client for host authority: {}", hostAuthority);
        HttpClient httpClient = new VertxHttpClientBuilder().vertx(vertx).build();
        SecretClientBuilder clientBuilder = configureClientBuilder(kvConfig,
                AzureQuarkusIdentifier.AZURE_QUARKUS_KEY_VAULT_SYNC_CLIENT,
                () -> new DefaultAzureCredentialBuilder().httpClient(httpClient).build());
        return clientBuilder
                .httpClient(httpClient)
                .vaultUrl("https://" + hostAuthority)
                .buildClient();
    }

    private Vertx createVertx() {
        return Vertx.vertx();
    }

    private void closeVertx(Vertx vertx) {
        try {
            vertx.close().toCompletionStage().toCompletableFuture().get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.emptyMap();
    }

    @Override
    public Set<String> getPropertyNames() {
        return Collections.emptySet();
    }

    @Override
    public String getValue(String propertyName) {
        if (!kvConfig.enabled() || kvConfig.endpoint().isEmpty()) {
            //the Key Vault integration is disabled or the endpoint is not set
            return null;
        }
        KeyVaultSecretReference reference = getSecretReference(propertyName, kvConfig.endpoint().orElse(""));
        if (reference == null) {
            // The propertyName is not in the form "kv//..." so return null.
            return null;
        }

        Vertx vertx = createVertx();
        vertx.createHttpClient();
        SecretClient client = createClient(reference.hostAuthority(), vertx);
        String secretValue;
        if (reference.secretVersion().isEmpty()) {
            secretValue = client.getSecret(reference.secretName()).getValue();
        } else {
            secretValue = client.getSecret(reference.secretName(), reference.secretVersion().get()).getValue();
        }

        closeVertx(vertx);
        return secretValue;
    }
}
