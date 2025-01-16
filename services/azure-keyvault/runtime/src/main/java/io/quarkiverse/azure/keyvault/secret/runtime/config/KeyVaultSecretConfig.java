package io.quarkiverse.azure.keyvault.secret.runtime.config;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.azure.keyvault.secret")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface KeyVaultSecretConfig {
    /**
     * The flag to enable the key vault secret. If set to false, the key vault secret will be disabled
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * The endpoint of Azure Key Vault Secret. Required if quarkus.azure.keyvault.secret.enabled is set to true
     */
    Optional<String> endpoint();
}
