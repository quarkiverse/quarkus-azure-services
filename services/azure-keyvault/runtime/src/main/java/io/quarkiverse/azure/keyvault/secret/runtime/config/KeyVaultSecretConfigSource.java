package io.quarkiverse.azure.keyvault.secret.runtime.config;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.azure.core.http.HttpClient;
import com.azure.core.http.vertx.VertxHttpClientBuilder;
import com.azure.core.util.ClientOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecretIdentifier;

import io.quarkiverse.azure.core.util.AzureQuarkusIdentifier;
import io.smallrye.config.common.AbstractConfigSource;
import io.vertx.core.Vertx;

class KeyVaultSecretConfigSource extends AbstractConfigSource {
    /** The ordinal is set to < 100 (which is the default) so that this config source is retrieved from last. */
    private static final int KEYVAULT_SECRET_ORDINAL = 50;

    private static final String CONFIG_SOURCE_NAME = "io.quarkiverse.azure.keyvault.secret.runtime.config";

    private final KeyVaultSecretConfig kvConfig;

    private final SecretClientBuilder builder;

    public KeyVaultSecretConfigSource(final KeyVaultSecretConfig kvConfig) {
        super(CONFIG_SOURCE_NAME, KEYVAULT_SECRET_ORDINAL);
        this.kvConfig = kvConfig;

        this.builder = new SecretClientBuilder()
                .clientOptions(
                        new ClientOptions().setApplicationId(AzureQuarkusIdentifier.AZURE_QUARKUS_KEY_VAULT_SYNC_CLIENT));
    }

    private SecretClient createClient(String vaultUrl, Vertx vertx) {
        HttpClient httpClient = new VertxHttpClientBuilder().vertx(vertx).build();
        return this.builder
                .credential(new DefaultAzureCredentialBuilder().httpClient(httpClient).build())
                .vaultUrl(vaultUrl).httpClient(httpClient).buildClient();
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
        KeyVaultSecretIdentifier secretIdentifier = KeyVaultSecretConfigUtil.getSecretIdentifier(propertyName,
                kvConfig.endpoint().orElse(""));
        if (secretIdentifier == null) {
            // The propertyName is not in the form "kv//..." so return null.
            return null;
        }

        Vertx vertx = createVertx();
        SecretClient client = createClient(secretIdentifier.getVaultUrl(), vertx);
        String secretValue;
        if (secretIdentifier.getVersion().equals("latest")) {
            secretValue = client.getSecret(secretIdentifier.getName()).getValue();
        } else {
            secretValue = client.getSecret(secretIdentifier.getName(), secretIdentifier.getVersion()).getValue();
        }

        closeVertx(vertx);
        return secretValue;
    }
}
