package io.quarkiverse.azure.servicebus.deployment;

import static io.quarkiverse.azure.servicebus.deployment.ServiceBusDevServicesConfig.EmulatorConfig.CONFIG_FILE_DIRECTORY;
import static io.quarkiverse.azure.servicebus.deployment.ServiceBusDevServicesConfig.EmulatorConfig.DEFAULT_CONFIG_FILE_NAME;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.testcontainers.utility.MountableFile;

class ServiceBusEmulatorConfigResolver {

    /**
     * Location in the classpath from where the fallback configuration file can be loaded.
     */
    private static final String FALLBACK_CONFIG_FILE_RESOURCE_PATH = "azure/servicebus-emulator/default-config.json";

    private final Path path;
    private final boolean exists;

    /**
     * Creates a {@link ServiceBusEmulatorConfigResolver} for the given configuration file path.
     *
     * @param configFilePath the config file path to search in the default configuration directory
     * @throws FileNotFoundException if a config file path was given and the file does not exist
     *
     * @see ServiceBusDevServicesConfig.EmulatorConfig#CONFIG_KEY_CONFIG_FILE_PATH
     */
    public ServiceBusEmulatorConfigResolver(Optional<String> configFilePath) throws FileNotFoundException {
        this(configFilePath, CONFIG_FILE_DIRECTORY);
    }

    /**
     * Creates a {@link ServiceBusEmulatorConfigResolver} for the given configuration file path in a custom
     * configuration directory.
     * For tests only.
     *
     * @param configFilePath the config file path to search in {@code configFileDirectory}
     * @param configFileDirectory the root directory for Azure Service Bus emulator configuration files,
     *        relative to the project root
     * @throws FileNotFoundException if a config file path was given and the file does not exist
     *
     * @see ServiceBusDevServicesConfig.EmulatorConfig#CONFIG_KEY_CONFIG_FILE_PATH
     */
    ServiceBusEmulatorConfigResolver(Optional<String> configFilePath, String configFileDirectory) throws FileNotFoundException {
        this.path = Path.of(configFileDirectory, configFilePath.orElse(DEFAULT_CONFIG_FILE_NAME));
        this.exists = Files.exists(path);
        if (configFilePath.isPresent() && !exists) {
            throw new FileNotFoundException(
                    String.format("Azure Service Bus emulator configuration file '%s' does not exist", path));
        }
    }

    /**
     * Gets the verified {@link Path} to an existing Azure Service Bus emulator configuration file.
     * <p>
     * The configuration file is resolved according to these rules:
     * <ul>
     * <li>If a config file path is configured and the config file exists, the path
     * to that file is returned.</li>
     * <li>If no config file path is configured but a config file exists at the
     * default location, the path to it is returned.</li>
     * <li>If no config file path is configured and no default config file exists,
     * an empty {@link Optional} is returned.</li>
     * </ul>
     *
     * @see ServiceBusDevServicesConfig.EmulatorConfig#CONFIG_KEY_CONFIG_FILE_PATH
     * @see ServiceBusDevServicesConfig.EmulatorConfig#DEFAULT_CONFIG_FILE_NAME
     * @see ServiceBusDevServicesConfig.EmulatorConfig#CONFIG_FILE_DIRECTORY
     */
    public Optional<Path> getConfigFile() {
        return exists ? Optional.of(path) : Optional.empty();
    }

    /**
     * Returns a basic configuration file for the Azure Service Bus emulator from the classpath.
     * <p>
     * This method provides a fallback configuration that will be used when no user-defined configuration file is present.
     * This allows the user to quickly get started with this extension without having to define a custom configuration.
     * <p>
     * The configuration file is loaded from the classpath location defined by {@link #FALLBACK_CONFIG_FILE_RESOURCE_PATH}.
     *
     * @return a {@link MountableFile} containing the default emulator configuration that can be mounted into a container.
     */
    public static MountableFile getFallbackConfiguration() {
        return MountableFile.forClasspathResource(FALLBACK_CONFIG_FILE_RESOURCE_PATH);
    }

}
