package io.quarkiverse.azure.keyvault.secret.runtime;

import java.util.Optional;
import java.util.function.Supplier;

import org.jboss.logging.Logger;

import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

import io.quarkiverse.azure.keyvault.secret.runtime.config.KeyVaultSecretConfig;
import io.quarkiverse.azure.keyvault.secret.runtime.config.KeyVaultSecretLocalConfig;
import io.quarkiverse.azure.keyvault.secret.runtime.config.KeyVaultSecretTokenConfig;
import io.quarkus.runtime.configuration.ConfigurationException;

public final class KeyVaultSecretClientBuilderUtil {

    private static final Logger log = Logger.getLogger(KeyVaultSecretClientBuilderUtil.class);

    private KeyVaultSecretClientBuilderUtil() {
        // prevent instantiation
    }

    public static SecretClientBuilder configureClientBuilder(
            KeyVaultSecretConfig secretConfiguration,
            String id,
            Supplier<DefaultAzureCredential> defaultAzureCredentialSupplier) {
        String endpoint = secretConfiguration.endpoint()
                .orElseThrow(() -> new ConfigurationException("The endpoint of Azure Key Vault Secret must be set"));
        SecretClientBuilder builder = new SecretClientBuilder()
                .clientOptions(new ClientOptions().setApplicationId(id))
                .vaultUrl(endpoint)
                .credential(getTokenCredential(secretConfiguration, defaultAzureCredentialSupplier));
        if (secretConfiguration.localConfiguration()
                .map(KeyVaultSecretLocalConfig::disableChallengeResourceVerification)
                .orElse(false)) {
            builder.disableChallengeResourceVerification();
        }
        return builder;
    }

    private static TokenCredential getTokenCredential(
            KeyVaultSecretConfig secretConfiguration,
            Supplier<DefaultAzureCredential> defaultAzureCredentialSupplier) {
        Optional<KeyVaultSecretLocalConfig> localConfig = secretConfiguration.localConfiguration();
        if (localConfig.isEmpty()) {
            return defaultAzureCredentialSupplier.get();
        }

        Optional<KeyVaultSecretTokenConfig> basicAuthConfig = localConfig.get().basicAuthentication();
        if (basicAuthConfig.isEmpty()) {
            return new DefaultAzureCredentialBuilder().build();
        }

        log.warn("Using basic authentication for Azure Key Vault Secret client");
        String username = basicAuthConfig.get().username();
        String password = basicAuthConfig.get().password();
        return new BasicAuthenticationCredential(username, password);
    }
}
