package io.quarkiverse.azure.app.configuration;

import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory.ConfigurableConfigSourceFactory;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AzureAppConfigurationConfigSourceFactory
        implements ConfigurableConfigSourceFactory<AzureAppConfigurationConfig> {

    @Override
    public Iterable<ConfigSource> getConfigSources(
            final ConfigSourceContext context,
            final AzureAppConfigurationConfig config) {

        Map<String, String> properties = getAzureAppConfiguration(config);
        return Collections.singleton(new AzureAppConfigurationConfigSource(properties));
    }

    private Map<String, String> getAzureAppConfiguration(final AzureAppConfigurationConfig config) {
        // We cannot use the Quarkus Vert.x instance, because the configuration executes before starting Vert.x
        //Vertx vertx = Vertx.vertx();
        //HttpClient vertxAsyncHttpClient = new VertxAsyncHttpClientBuilder().vertx(vertx).build();

        HttpClient nettyAsyncHttpClient = new NettyAsyncHttpClientBuilder().build();

        ConfigurationClientBuilder clientBuilder = new ConfigurationClientBuilder()
                .httpClient(nettyAsyncHttpClient)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.NONE))
                .connectionString(config.connectionString());

        ConfigurationClient client = clientBuilder.buildClient();

        Map<String, String> properties = new LinkedHashMap<>(); // LinkedHashMap for reproducible ordering
        PagedIterable<ConfigurationSetting> listConfigurationSettings = client.listConfigurationSettings(new SettingSelector());
        listConfigurationSettings.forEach(new Consumer<ConfigurationSetting>() {
            @Override
            public void accept(final ConfigurationSetting configurationSetting) {
                properties.put(configurationSetting.getKey(), configurationSetting.getValue());
            }
        });

        // TODO renove if we don't use Vertx       try {
        //            vertx.close().toCompletionStage().toCompletableFuture().get();
        //        } catch (Exception e) {
        //            throw new RuntimeException(e);
        //        }

        return properties;
    }
}
