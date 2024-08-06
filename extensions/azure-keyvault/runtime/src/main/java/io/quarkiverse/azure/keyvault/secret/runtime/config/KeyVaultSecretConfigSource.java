package io.quarkiverse.azure.keyvault.secret.runtime.config;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.azure.core.util.ClientOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecretIdentifier;

import io.quarkiverse.azure.core.util.AzureQuarkusIdentifier;
import io.smallrye.config.common.AbstractConfigSource;

public class KeyVaultSecretConfigSource extends AbstractConfigSource {
    /** The ordinal is set to < 100 (which is the default) so that this config source is retrieved from last. */
    private static final int KEYVAULT_SECRET_ORDINAL = 50;

    private static final String CONFIG_SOURCE_NAME = "io.quarkiverse.azure.keyvault.secret.runtime.config";

    private final KeyVaultSecretConfig kvConfig;

    private final SecretClientBuilder builder;

    public KeyVaultSecretConfigSource(final KeyVaultSecretConfig kvConfig) {
        super(CONFIG_SOURCE_NAME, KEYVAULT_SECRET_ORDINAL);
        this.kvConfig = kvConfig;

        this.builder = new SecretClientBuilder()
                .clientOptions(new ClientOptions().setApplicationId(AzureQuarkusIdentifier.AZURE_QUARKUS_KEY_VAULT_SYNC_CLIENT))
                .credential(new DefaultAzureCredentialBuilder().build());
    }

    private static SecretClient createClient(String vaultUrl) {
        return builder.vaultUrl(vaultUrl).buildClient();
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

        SecretClient client = createClient(secretIdentifier.getVaultUrl());
        if (secretIdentifier.getVersion().equals("latest")) {
            return client.getSecret(secretIdentifier.getName()).getValue();
        }

        return client.getSecret(secretIdentifier.getName(), secretIdentifier.getVersion()).getValue();
    }
}
