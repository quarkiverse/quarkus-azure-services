package io.quarkiverse.azure.keyvault.secret.runtime.config;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import org.eclipse.microprofile.config.spi.ConfigSource;

import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;

public class KeyVaultSecretConfigSourceFactory implements
        ConfigSourceFactory.ConfigurableConfigSourceFactory<KeyVaultSecretConfig> {

    @Override
    public Iterable<ConfigSource> getConfigSources(
            final ConfigSourceContext configSourceContext,
            final KeyVaultSecretConfig keyVaultSecretConfig) {

        if (keyVaultSecretConfig.enabled()) {
            return singletonList(new KeyVaultSecretConfigSource(keyVaultSecretConfig));
        }
        return emptyList();
    }
}
