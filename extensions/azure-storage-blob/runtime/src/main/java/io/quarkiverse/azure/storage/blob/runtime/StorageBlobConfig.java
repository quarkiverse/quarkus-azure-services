package io.quarkiverse.azure.storage.blob.runtime;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "azure.storage.blob", phase = ConfigPhase.RUN_TIME)
public class StorageBlobConfig {

    /**
     * The connection string of Azure Storage Account.
     */
    @ConfigItem
    public String connectionString;
}
