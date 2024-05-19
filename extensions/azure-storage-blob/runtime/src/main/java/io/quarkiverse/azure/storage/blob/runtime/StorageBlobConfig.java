package io.quarkiverse.azure.storage.blob.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "azure.storage.blob", phase = ConfigPhase.RUN_TIME)
public class StorageBlobConfig {

    /**
     * The flag to enable the storage blob. If set to false, the storage blob will be disabled
     */
    @ConfigItem(defaultValue = "true")
    public boolean enabled;

    /**
     * The connection string of Azure Storage Account. Required if quarkus.azure.storage.blob.enabled is set to true
     */
    @ConfigItem
    public Optional<String> connectionString;
}
