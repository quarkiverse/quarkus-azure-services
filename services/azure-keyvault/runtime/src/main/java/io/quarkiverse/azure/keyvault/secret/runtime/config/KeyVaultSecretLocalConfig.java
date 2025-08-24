package io.quarkiverse.azure.keyvault.secret.runtime.config;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface KeyVaultSecretLocalConfig {

    /**
     * The flag to disable the challenge resource verification. If set to false, the verification remains enabled.
     */
    @WithDefault("false")
    boolean disableChallengeResourceVerification();

    /**
     * The configuration to use a basic authentication token.<br/>
     * WARNING! Not recommended for production use.
     */
    Optional<KeyVaultSecretTokenConfig> basicAuthentication();

}
