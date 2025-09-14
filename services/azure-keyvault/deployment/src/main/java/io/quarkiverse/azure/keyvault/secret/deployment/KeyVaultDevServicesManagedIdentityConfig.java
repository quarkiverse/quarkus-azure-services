package io.quarkiverse.azure.keyvault.secret.deployment;

import io.quarkus.runtime.annotations.ConfigGroup;

@ConfigGroup
public interface KeyVaultDevServicesManagedIdentityConfig {

    /**
     * Specify the token port for the managed identity server.
     */
    int tokenPort();

}
