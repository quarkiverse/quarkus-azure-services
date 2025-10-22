package io.quarkiverse.azure.app.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.microprofile.config.spi.ConfigSource;

import com.azure.core.http.HttpClient;
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

    /*
     * The maximum number of labels that can be specified in a single request.
     */
    private static final int MAX_LABELS_PER_REQUEST = 5;

    private static List<SettingSelector> getSettingSelector(final AzureAppConfigurationConfig config) {
        if (config.labels().isEmpty()) {
            return List.of(new SettingSelector());
        } else {
            List<String> allLabels = Arrays.asList(config.labels().get().split(","));
            List<List<String>> labelsPartitions = partition(allLabels, MAX_LABELS_PER_REQUEST);
            List<SettingSelector> settingSelectors = new ArrayList<>(labelsPartitions.size());
            for (List<String> settingSelector : labelsPartitions) {
                settingSelectors.add(new SettingSelector().setLabelFilter(String.join(",", settingSelector)));
            }
            return settingSelectors;
        }
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
                .orElseThrow(() -> new ConfigurationException(
                        "The endpoint of the app configuration (quarkus.azure.app.configuration.endpoint) must be set"));

        // We cannot use the Quarkus Vert.x instance, because the configuration executes before starting Vert.x
        Vertx vertx = Vertx.vertx();
        HttpClient httpClient = new VertxHttpClientBuilder().vertx(vertx).build();

        ConfigurationClientBuilder clientBuilder = new ConfigurationClientBuilder()
                .clientOptions(new ClientOptions().setApplicationId(AzureQuarkusIdentifier.AZURE_QUARKUS_APP_CONFIGURATION))
                .httpClient(httpClient);
        if (!config.connectionString().isEmpty()) {
            clientBuilder.connectionString(config.connectionString());
        } else {
            clientBuilder.endpoint(endpoint).credential(new DefaultAzureCredentialBuilder().httpClient(httpClient).build());
        }
        ConfigurationClient client = clientBuilder.buildClient();

        Map<String, String> properties = new LinkedHashMap<>(); // LinkedHashMap for reproducible ordering

        Consumer<ConfigurationSetting> consumer = new Consumer<>() {
            @Override
            public void accept(final ConfigurationSetting configurationSetting) {
                properties.put(configurationSetting.getKey(), configurationSetting.getValue());
            }
        };
        List<SettingSelector> settingSelectors = getSettingSelector(config);
        for (SettingSelector settingSelector : settingSelectors) {
            PagedIterable<ConfigurationSetting> listConfigurationSettings = client
                    .listConfigurationSettings(settingSelector);
            listConfigurationSettings.forEach(consumer);
        }

        try {
            vertx.close().toCompletionStage().toCompletableFuture().get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return properties;
    }

    private static List<List<String>> partition(List<String> list, int chunkSize) {
        List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            chunks.add(list.subList(i, Math.min(list.size(), i + chunkSize)));
        }
        return chunks;
    }
}
