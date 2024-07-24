package io.quarkiverse.azure.keyvault.secret.runtime.config;

import io.quarkiverse.azure.keyvault.secret.runtime.KeyVaultSecretConfig;
import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigSource;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class KeyVaultSecretConfigSourceFactory implements ConfigSourceFactory {
    @Override
    public Iterable<ConfigSource> getConfigSources(ConfigSourceContext configSourceContext) {
        SmallRyeConfig config = new SmallRyeConfigBuilder()
                .withSources(new ConfigSourceContext.ConfigSourceContextConfigSource(configSourceContext))
                .withMapping(KeyVaultSecretConfig.class)
                .withMappingIgnore("quarkus.**")
                .build();

        KeyVaultSecretConfig kvConfig = config.getConfigMapping(KeyVaultSecretConfig.class);

        if(kvConfig.enabled){
            return singletonList(new KeyVaultSecretConfigSource(kvConfig));
        }

        return emptyList();
    }
}
