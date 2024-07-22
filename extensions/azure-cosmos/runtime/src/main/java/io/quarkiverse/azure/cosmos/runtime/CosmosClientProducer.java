package io.quarkiverse.azure.cosmos.runtime;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

import io.quarkiverse.azure.core.util.AzureQuarkusIdentifier;

public class CosmosClientProducer {

    @Inject
    CosmosConfig cosmosConfiguration;

    @Produces
    public CosmosClient createCosmosClient() {
        if (!cosmosConfiguration.enabled) {
            return null;
        }

        assert cosmosConfiguration.endpoint.isPresent() : "The endpoint of Azure Cosmos DB must be set";
        return new CosmosClientBuilder()
                .userAgentSuffix(AzureQuarkusIdentifier.AZURE_QUARKUS_COSMOS)
                .endpoint(cosmosConfiguration.endpoint.get())
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
    }

    @Produces
    public CosmosAsyncClient createCosmosAsyncClient() {
        if (!cosmosConfiguration.enabled) {
            return null;
        }

        assert cosmosConfiguration.endpoint.isPresent() : "The endpoint of Azure Cosmos DB must be set";
        return new CosmosClientBuilder()
                .userAgentSuffix(AzureQuarkusIdentifier.AZURE_QUARKUS_COSMOS)
                .endpoint(cosmosConfiguration.endpoint.get())
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildAsyncClient();
    }
}
