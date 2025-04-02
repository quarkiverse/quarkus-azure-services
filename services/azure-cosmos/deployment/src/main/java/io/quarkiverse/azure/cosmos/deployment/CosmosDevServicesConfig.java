package io.quarkiverse.azure.cosmos.deployment;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface CosmosDevServicesConfig {

    /**
     * If Dev Services for Azure Cosmos DB has been explicitly enabled or disabled.
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * Indicates if the Cosmos instance managed by Quarkus Dev Services is shared.
     * When shared, Quarkus looks for running containers using label-based service discovery.
     * If a matching container is found, it is used, and so a second one is not started.
     * Otherwise, Dev Services for Azure Storage Blob starts a new container.
     * <p>
     * The discovery uses the {@code quarkus-dev-service-azure-cosmos} label.
     * The value is configured using the {@code service-name} property.
     * <p>
     * Container sharing is only used in dev mode.
     */
    @WithDefault("true")
    boolean shared();

    /**
     * The value of the {@code quarkus-dev-service-azure-cosmos} label attached to the started container.
     * This property is used when {@code shared} is set to {@code true}.
     * In this case, before starting a container, Dev Services for Azure Storage Blob looks for a container with the
     * {@code quarkus-dev-service-azure-cosmos} label
     * set to the configured value. If found, it will use this container instead of starting a new one. Otherwise it
     * starts a new container with the {@code quarkus-dev-service-azure-cosmos} label set to the specified value.
     * <p>
     * This property is used when you need multiple shared azurite instances.
     */
    @WithDefault("default-cosmos")
    String serviceName();
}
