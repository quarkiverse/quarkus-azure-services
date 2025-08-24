package io.quarkiverse.azure.keyvault.secret.deployment;

import java.util.Map;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface KeyVaultDevServicesConfig {

    String DEFAULT_IMAGE_NAME = "nagyesta/lowkey-vault:4.0.0-ubi9-minimal";

    /**
     * If Dev Services for Azure Key Vault has been explicitly enabled or disabled.
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * The container image name of Lowkey Vault.
     * See the <a href="https://hub.docker.com/r/nagyesta/lowkey-vault/tags">artifact
     * registry</a> for available tags of the default image.
     * <p>
     * This extension has been tested and verified working with version
     * {@code nagyesta/lowkey-vault:4.0.0-ubi9-minimal}.
     */
    @WithDefault(DEFAULT_IMAGE_NAME)
    String imageName();

    /**
     * Set if we want to use managed identity for authentication.
     * If not set, we will default to username- and password-based authentication.
     */
    Optional<KeyVaultDevServicesManagedIdentityConfig> managedIdentity();

    /**
     * If we want to automatically merge the Lowkey Vault SSL keystore with the application keystore.
     */
    @WithDefault("true")
    boolean mergeSslKeystoreWithApplicationKeystore();

    /**
     * A map of secret names and values to pre-set in the Lowkey Vault instance.
     */
    Map<String, String> preSetSecrets();

}
