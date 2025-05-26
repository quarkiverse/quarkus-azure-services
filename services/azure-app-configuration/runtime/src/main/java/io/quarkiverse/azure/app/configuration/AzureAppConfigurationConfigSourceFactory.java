package io.quarkiverse.azure.app.configuration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.microprofile.config.spi.ConfigSource;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.vertx.VertxHttpClientBuilder;
import com.azure.core.util.ClientOptions;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.identity.DefaultAzureCredentialBuilder;

import io.quarkiverse.azure.core.util.AzureQuarkusIdentifier;
import io.quarkus.runtime.configuration.ConfigurationException;
import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory.ConfigurableConfigSourceFactory;
import io.vertx.core.Vertx;

public class AzureAppConfigurationConfigSourceFactory
        implements ConfigurableConfigSourceFactory<AzureAppConfigurationConfig> {

    private static SettingSelector getSettingSelector(final AzureAppConfigurationConfig config) {
        var settingSelector = new SettingSelector();
        config.labels().ifPresent(settingSelector::setLabelFilter);

        return settingSelector;
    }

    @Override
    public Iterable<ConfigSource> getConfigSources(
            final ConfigSourceContext context,
            final AzureAppConfigurationConfig config) {

        Map<String, String> properties = getAzureAppConfiguration(config);
        return Collections.singleton(new AzureAppConfigurationConfigSource(properties));
    }

    private Map<String, String> getAzureAppConfiguration(final AzureAppConfigurationConfig config) {
        // Return an empty map if the app configuration is disabled
        if (!config.enabled()) {
            return Collections.emptyMap();
        }
        String endpoint = config.endpoint()
                .orElseThrow(() -> new ConfigurationException("The endpoint of the app configuration must be set"));

        // We cannot use the Quarkus Vert.x instance, because the configuration executes before starting Vert.x
        Vertx vertx = Vertx.vertx();
        VertxHttpClientBuilder httpClientBuilder = new VertxHttpClientBuilder().vertx(vertx);

        ConfigurationClientBuilder clientBuilder = new ConfigurationClientBuilder()
                .clientOptions(new ClientOptions().setApplicationId(AzureQuarkusIdentifier.AZURE_QUARKUS_APP_CONFIGURATION))
                .httpClient(httpClientBuilder.build());
        if (!config.connectionString().isEmpty()) {
            clientBuilder.connectionString(config.connectionString());
        } else {
            clientBuilder.endpoint(endpoint).credential(new DefaultAzureCredentialBuilder().build());
        }
        ConfigurationClient client = clientBuilder.buildClient();

        Map<String, String> properties = new LinkedHashMap<>(); // LinkedHashMap for reproducible ordering
        PagedIterable<ConfigurationSetting> listConfigurationSettings = client
                .listConfigurationSettings(getSettingSelector(config));
        listConfigurationSettings.forEach(new Consumer<ConfigurationSetting>() {
            @Override
            public void accept(final ConfigurationSetting configurationSetting) {
                properties.put(configurationSetting.getKey(), configurationSetting.getValue());
            }
        });

        try {
            vertx.close().toCompletionStage().toCompletableFuture().get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return properties;
    }
}
