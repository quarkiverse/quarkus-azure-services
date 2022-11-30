package io.quarkiverse.azure.app.configuration;

import java.util.Map;

import io.smallrye.config.common.MapBackedConfigSource;

public class AzureAppConfigurationConfigSource extends MapBackedConfigSource {
    public AzureAppConfigurationConfigSource(final Map<String, String> properties) {
        super(AzureAppConfigurationConfigSource.class.getSimpleName(), properties, 200);
    }
}
