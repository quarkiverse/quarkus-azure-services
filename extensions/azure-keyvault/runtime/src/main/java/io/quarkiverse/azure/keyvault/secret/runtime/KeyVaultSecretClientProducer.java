package io.quarkiverse.azure.keyvault.secret.runtime;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.azure.core.util.ClientOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

import io.quarkiverse.azure.core.util.AzureQuarkusIdentifier;

public class KeyVaultSecretClientProducer {

    @Inject
    KeyVaultSecretConfig secretConfiguration;

    @Produces
    public SecretClient createSecretClient() {
        return new SecretClientBuilder()
                .clientOptions(new ClientOptions().setApplicationId(AzureQuarkusIdentifier.AZURE_QUARKUS_KEY_VAULT_SYNC_CLIENT))
                .vaultUrl(secretConfiguration.endpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
    }

    @Produces
    public SecretAsyncClient createSecretAsyncClient() {
        return new SecretClientBuilder()
                .clientOptions(
                        new ClientOptions().setApplicationId(AzureQuarkusIdentifier.AZURE_QUARKUS_KEY_VAULT_ASYNC_CLIENT))
                .vaultUrl(secretConfiguration.endpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildAsyncClient();
    }
}
