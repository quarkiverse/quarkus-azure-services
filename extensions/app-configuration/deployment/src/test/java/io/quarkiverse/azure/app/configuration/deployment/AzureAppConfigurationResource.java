package io.quarkiverse.azure.app.configuration.deployment;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class AzureAppConfigurationResource implements QuarkusTestResourceLifecycleManager {
    private HttpServer httpServer;

    @Override
    public Map<String, String> start() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(8082), 0);
            httpServer.createContext("/kv", new HttpHandler() {
                @Override
                public void handle(final HttpExchange exchange) throws IOException {
                    URL resource = Thread.currentThread().getContextClassLoader().getResource("response.json");
                    if (resource == null) {
                        exchange.sendResponseHeaders(400, 0);
                        return;
                    }

                    String body = IOUtils.toString(resource, StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type",
                            "application/vnd.microsoft.appconfig.kvset+json; charset=utf-8");
                    exchange.sendResponseHeaders(200, body.length());
                    exchange.getResponseBody().write(body.getBytes());
                    exchange.getResponseBody().close();
                }
            });
            httpServer.start();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }
}
