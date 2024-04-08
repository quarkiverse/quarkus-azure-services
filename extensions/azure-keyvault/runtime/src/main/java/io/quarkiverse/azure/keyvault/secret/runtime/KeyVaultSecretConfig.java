package io.quarkiverse.azure.keyvault.secret.runtime;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "azure.keyvault.secret", phase = ConfigPhase.RUN_TIME)
public class KeyVaultSecretConfig {

    /**
     * The endpoint of Azure Key Vault Secret.
     */
    @ConfigItem
    public String endpoint;
}
