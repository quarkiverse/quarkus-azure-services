package io.quarkiverse.azure.app.configuration.it;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
        int port = 8082;
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
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

        return Map.of(
                "quarkus.azure.app.configuration.endpoint", "http://localhost:" + port,
                /* Our server does not validate the credentials anyway */
                "quarkus.azure.app.configuration.id", "dummy",
                "quarkus.azure.app.configuration.secret", "aGVsbG8=" // Base64 encoded "hello"
        );
    }

    @Override
    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }
}
