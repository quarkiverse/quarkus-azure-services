package io.quarkiverse.azure.servicebus.deployment;

import static io.quarkiverse.azure.servicebus.deployment.ServiceBusDevServicesConfig.EmulatorConfig.DEFAULT_CONFIG_FILE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.utility.MountableFile;

class ServiceBusEmulatorConfigResolverTest {

    @Test
    @DisplayName("Path unconfigured and no file exists at default location")
    void noDefaultFileExists() throws FileNotFoundException {
        var configResolver = new ServiceBusEmulatorConfigResolver(Optional.empty(),
                "src/test/resources/servicebus-emulator-empty");

        assertThat(configResolver.getConfigFile())
                .isEmpty();
    }

    @Test
    @DisplayName("Path unconfigured but file exists at default location")
    void fileExistsInDefaultLocation() throws FileNotFoundException {
        var configResolver = new ServiceBusEmulatorConfigResolver(Optional.empty(),
                "src/test/resources/servicebus-emulator");

        assertThat(configResolver.getConfigFile())
                .hasValueSatisfying(path -> assertThat(path)
                        .endsWith(Path.of("servicebus-emulator/config.json")));
    }

    @Test
    @DisplayName("Path configured to existing file at default location")
    void fileExistsInDefaultLocationAndIsConfigured() throws FileNotFoundException {
        var configResolver = new ServiceBusEmulatorConfigResolver(Optional.of(DEFAULT_CONFIG_FILE_NAME),
                "src/test/resources/servicebus-emulator");

        assertThat(configResolver.getConfigFile())
                .hasValueSatisfying(path -> assertThat(path)
                        .endsWith(Path.of("servicebus-emulator/config.json")));
    }

    @Test
    @DisplayName("Path configured to existing file at custom location")
    void customFileExists() throws FileNotFoundException {
        var configResolver = new ServiceBusEmulatorConfigResolver(Optional.of("custom-config.json"),
                "src/test/resources/servicebus-emulator");

        assertThat(configResolver.getConfigFile())
                .hasValueSatisfying(path -> assertThat(path)
                        .endsWith(Path.of("servicebus-emulator/custom-config.json")));
    }

    @Test
    @DisplayName("Path configured to non-existent file at default location")
    void customFileAtDefaultLocationDoesNotExist() {
        assertThatExceptionOfType(FileNotFoundException.class)
                .isThrownBy(
                        () -> new ServiceBusEmulatorConfigResolver(Optional.of(DEFAULT_CONFIG_FILE_NAME),
                                "src/test/resources/servicebus-emulator-empty"));
    }

    @Test
    @DisplayName("Path configured to non-existent file at custom location")
    void customFileDoesNotExist() {
        assertThatExceptionOfType(FileNotFoundException.class)
                .isThrownBy(
                        () -> new ServiceBusEmulatorConfigResolver(Optional.of("does-not-exist"),
                                "src/test/resources/servicebus-emulator"));
    }

    @Test
    @DisplayName("Fallback configuration is loaded from the classpath")
    void fallbackConfiguration() {
        MountableFile fallbackConfiguration = ServiceBusEmulatorConfigResolver.getFallbackConfiguration();

        assertThat(fallbackConfiguration.getResolvedPath())
                .endsWith("default-config.json");
    }
}
