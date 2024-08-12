package io.quarkiverse.azure.storage.blob.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.azure.storage.blob")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface StorageBlobConfig {

    /**
     * The flag to enable the storage blob. If set to false, the storage blob will be disabled
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * The connection string of Azure Storage Account. Required if quarkus.azure.storage.blob.enabled is set to true
     */
    Optional<String> connectionString();
}
