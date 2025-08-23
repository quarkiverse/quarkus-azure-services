package io.quarkiverse.azure.keyvault.secret.runtime.config;

import io.quarkus.runtime.annotations.ConfigGroup;

@ConfigGroup
public interface KeyVaultSecretTokenConfig {

    /**
     * The username part of the basic authentication token.
     */
    String username();

    /**
     * The password part of the basic authentication token.
     */
    String password();

}
