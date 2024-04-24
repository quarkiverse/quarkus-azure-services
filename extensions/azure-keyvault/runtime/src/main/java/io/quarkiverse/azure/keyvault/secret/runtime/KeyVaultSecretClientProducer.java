package io.quarkiverse.azure.keyvault.secret.runtime;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

public class KeyVaultSecretClientProducer {

    @Inject
    KeyVaultSecretConfig secretConfiguration;

    @Produces
    public SecretClient createSecretClient() {
        return new SecretClientBuilder()
                .vaultUrl(secretConfiguration.endpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
    }

    @Produces
    public SecretAsyncClient createSecretAsyncClient() {
        return new SecretClientBuilder()
                .vaultUrl(secretConfiguration.endpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildAsyncClient();
    }
}
