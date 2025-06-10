package io.quarkiverse.azure.servicebus.deployment;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;

import io.quarkus.test.QuarkusUnitTest;

class DeploymentWithEnabledProducersTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addAsResource(
                            new StringAsset(
                                    """
                                            # supply required configuration for the CDI producer
                                            quarkus.azure.servicebus.namespace=some.namespace
                                            # disable the Dev Services to avoid potential configuration issues to interfere with the injection tests
                                            quarkus.azure.servicebus.devservices.enabled=false
                                            """),
                            "application.properties"));

    @Inject
    ServiceBusClientBuilder serviceBusClientBuilder;

    @Test
    void cdiProducerWorks() {
        // serviceBusClientBuilder should be available when the extension is enabled
        if (serviceBusClientBuilder == null) {
            throw new AssertionError("ServiceBusClientBuilder should be available when the extension is enabled.");
        }
    }
}
