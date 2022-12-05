package io.quarkiverse.azure.app.configuration;

import io.quarkus.runtime.configuration.ConfigBuilder;
import io.smallrye.config.SmallRyeConfigBuilder;

public class AzureAppConfigurationConfigBuilder implements ConfigBuilder {
    @Override
    public SmallRyeConfigBuilder configBuilder(final SmallRyeConfigBuilder builder) {
        return builder.withSources(new AzureAppConfigurationConfigSourceFactory());
    }
}
