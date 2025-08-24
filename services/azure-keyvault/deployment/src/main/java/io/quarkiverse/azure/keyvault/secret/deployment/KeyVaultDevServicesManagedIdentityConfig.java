package io.quarkiverse.azure.keyvault.secret.deployment;

import io.quarkus.runtime.annotations.ConfigGroup;

@ConfigGroup
public interface KeyVaultDevServicesManagedIdentityConfig {

    /**
     * Set if we want to specify the token port for the managed identity token server.
     */
    int tokenPort();

}
