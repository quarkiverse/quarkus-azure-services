package io.quarkiverse.azure.keyvault.secret.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "azure.keyvault.secret", phase = ConfigPhase.RUN_TIME)
public class KeyVaultSecretConfig {

    /**
     * The flag to enable the key vault secret. If set to false, the key vault secret will be disabled
     */
    @ConfigItem(defaultValue = "true")
    public boolean enabled;

    /**
     * The endpoint of Azure Key Vault Secret. Required if quarkus.azure.keyvault.secret.enabled is set to true
     */
    @ConfigItem
    public Optional<String> endpoint;
}
