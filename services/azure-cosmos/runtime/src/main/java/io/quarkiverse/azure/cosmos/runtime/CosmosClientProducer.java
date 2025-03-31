package io.quarkiverse.azure.cosmos.runtime;

import java.util.Objects;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

import io.quarkiverse.azure.core.util.AzureQuarkusIdentifier;

public class CosmosClientProducer {

    @Inject
    CosmosConfig cosmosConfiguration;

    @Produces
    public CosmosClient createCosmosClient() {
        CosmosClientBuilder builder = getBuilder();

        if (builder == null) {
            return null;
        } else {
            String emulatorMode = Objects.requireNonNullElse(
                    System.getProperty("COSMOS.EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED"), "false");
            if (emulatorMode.equals("true")) {
                builder = builder.gatewayMode();
            }
            return builder.buildClient();
        }
    }

    private CosmosClientBuilder getBuilder() {
        if (!cosmosConfiguration.enabled()) {
            return null;
        }

        assert cosmosConfiguration.endpoint().isPresent() : "The endpoint of Azure Cosmos DB must be set";
        CosmosClientBuilder builder = new CosmosClientBuilder()
                .userAgentSuffix(AzureQuarkusIdentifier.AZURE_QUARKUS_COSMOS)
                .endpoint(cosmosConfiguration.endpoint().get());
        if (cosmosConfiguration.key().isPresent()) {
            builder.key(cosmosConfiguration.key().get());
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
        return builder;
    }
}
