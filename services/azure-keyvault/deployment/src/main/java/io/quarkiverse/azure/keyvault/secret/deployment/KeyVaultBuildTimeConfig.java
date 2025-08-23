package io.quarkiverse.azure.keyvault.secret.deployment;

import static io.quarkiverse.azure.keyvault.secret.deployment.KeyVaultBuildTimeConfig.PREFIX;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigRoot
@ConfigMapping(prefix = PREFIX)
public interface KeyVaultBuildTimeConfig {

    String PREFIX = "quarkus.azure.keyvault";

    /**
     * Dev Services configuration.
     */
    KeyVaultDevServicesConfig devservices();
}
