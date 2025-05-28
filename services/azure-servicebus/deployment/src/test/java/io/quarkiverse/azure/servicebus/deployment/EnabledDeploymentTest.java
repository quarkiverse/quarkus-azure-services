package io.quarkiverse.azure.servicebus.deployment;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;

import io.quarkus.test.QuarkusUnitTest;

class EnabledDeploymentTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addAsResource(new StringAsset(
                            "quarkus.azure.servicebus.connection-string=Endpoint=sb://localhost;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;"),
                            "application.properties"));

    @Inject
    ServiceBusClientBuilder serviceBusClientBuilder;

    @Test
    void cdiProducerWorks() {

    }
}
