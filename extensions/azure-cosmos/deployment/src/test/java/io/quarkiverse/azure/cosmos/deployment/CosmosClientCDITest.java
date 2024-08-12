package io.quarkiverse.azure.cosmos.deployment;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;

@QuarkusTestResource(CosmosTestResource.class)
public class CosmosClientCDITest {

    @Inject
    CosmosAsyncClient cosmosAsyncClient;

    @Inject
    CosmosClient cosmosClient;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    @Test
    public void test() {
        // should finish with success
    }
}
