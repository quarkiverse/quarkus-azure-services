package io.quarkiverse.azure.keyvault.secret.deployment;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigRoot
@ConfigMapping(prefix = KeyVaultBuildTimeConfig.PREFIX)
public interface KeyVaultBuildTimeConfig {

    String PREFIX = "quarkus.azure.keyvault";

    /**
     * Dev Services configuration.
     */
    KeyVaultDevServicesConfig devservices();
}
