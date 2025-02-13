package io.quarkiverse.azure.storage.blob.deployment;

import java.util.Optional;
import java.util.OptionalInt;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface StorageBlobDevServicesConfig {

    /**
     * If DevServices has been explicitly enabled or disabled. DevServices is generally enabled
     * by default, unless there is an existing configuration present.
     * <p>
     * When DevServices is enabled Quarkus will attempt to automatically configure and start
     * an azurite instance when running in Dev or Test mode and when Docker is running.
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * The container image name to use, for container based DevServices providers.
     */
    @WithDefault(DevServicesStorageBlobProcessor.IMAGE)
    Optional<String> imageName();

    /**
     * Optional fixed port the Dev services will listen to.
     * <p>
     * If not defined, the port will be chosen randomly.
     */
    OptionalInt port();

    /**
     * Indicates if the azurite instance managed by Quarkus Dev Services is shared.
     * When shared, Quarkus looks for running containers using label-based service discovery.
     * If a matching container is found, it is used, and so a second one is not started.
     * Otherwise, Dev Services for Azure Storage Blob starts a new container.
     * <p>
     * The discovery uses the {@code quarkus-dev-service-azure-storage-blob} label.
     * The value is configured using the {@code service-name} property.
     * <p>
     * Container sharing is only used in dev mode.
     */
    @WithDefault("true")
    boolean shared();

    /**
     * The value of the {@code quarkus-dev-service-azure-storage-blob} label attached to the started container.
     * This property is used when {@code shared} is set to {@code true}.
     * In this case, before starting a container, Dev Services for Azure Storage Blob looks for a container with the
     * {@code quarkus-dev-service-azure-storage-blob} label
     * set to the configured value. If found, it will use this container instead of starting a new one. Otherwise it
     * starts a new container with the {@code quarkus-dev-service-azure-storage-blob} label set to the specified value.
     * <p>
     * This property is used when you need multiple shared azurite instances.
     */
    @WithDefault("default-storage-blob")
    String serviceName();
}
