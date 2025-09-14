package io.quarkiverse.azure.keyvault.secret.runtime;

import static io.quarkiverse.azure.keyvault.secret.runtime.KeyVaultSecretClientBuilderUtil.configureClientBuilder;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;

import io.quarkiverse.azure.core.util.AzureQuarkusIdentifier;
import io.quarkiverse.azure.keyvault.secret.runtime.config.KeyVaultSecretConfig;

public class KeyVaultSecretClientProducer {

    @Inject
    KeyVaultSecretConfig secretConfiguration;

    @Produces
    public SecretClient createSecretClient() {
        if (!secretConfiguration.enabled()) {
            return null;
        }

        return configureClientBuilder(secretConfiguration,
                AzureQuarkusIdentifier.AZURE_QUARKUS_KEY_VAULT_SYNC_CLIENT,
                new DefaultAzureCredentialBuilder()::build).buildClient();
    }

    @Produces
    public SecretAsyncClient createSecretAsyncClient() {
        if (!secretConfiguration.enabled()) {
            return null;
        }
        return configureClientBuilder(secretConfiguration,
                AzureQuarkusIdentifier.AZURE_QUARKUS_KEY_VAULT_ASYNC_CLIENT,
                new DefaultAzureCredentialBuilder()::build).buildAsyncClient();
    }
}
