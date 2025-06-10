package io.quarkiverse.azure.servicebus.deployment;

import jakarta.enterprise.inject.UnsatisfiedResolutionException;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;

import io.quarkus.test.QuarkusUnitTest;

class DeploymentWithDisabledProducersTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setExpectedException(UnsatisfiedResolutionException.class)
            .withApplicationRoot((jar) -> jar
                    .addAsResource(
                            new StringAsset(
                                    """
                                            # disable the CDI producers of the extension
                                            quarkus.azure.servicebus.enabled=false
                                            # disable the Dev Services to avoid potential configuration issues to interfere with the injection tests
                                            quarkus.azure.servicebus.devservices.enabled=false
                                            """),
                            "application.properties"));

    @Inject
    ServiceBusClientBuilder serviceBusClientBuilder;

    @Test
    void disabledCdiProducersLetDeploymentFail() {
        // serviceBusClientBuilder is expected to be null
        if (serviceBusClientBuilder != null) {
            throw new AssertionError("ServiceBusClientBuilder should not be available when the extension is disabled.");
        }
    }
}
