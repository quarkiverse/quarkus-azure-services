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
        CosmosClientBuilder builder = getBuilder();
        return null == builder ? null : builder.buildClient();
    }

    @Produces
    public CosmosAsyncClient createCosmosAsyncClient() {
        CosmosClientBuilder builder = getBuilder();
        return null == builder ? null : builder.buildAsyncClient();
    }

    private CosmosClientBuilder getBuilder() {
        if (!cosmosConfiguration.enabled) {
            return null;
        }

        assert cosmosConfiguration.endpoint.isPresent() : "The endpoint of Azure Cosmos DB must be set";
        CosmosClientBuilder builder = new CosmosClientBuilder()
                .userAgentSuffix(AzureQuarkusIdentifier.AZURE_QUARKUS_COSMOS)
                .endpoint(cosmosConfiguration.endpoint.get());
        if (cosmosConfiguration.key.isPresent()) {
            builder.key(cosmosConfiguration.key.get());
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
        return builder;
    }
}
