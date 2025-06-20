package io.quarkiverse.azure.servicebus.deployment;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig;
import io.quarkus.test.QuarkusUnitTest;

/**
 * If no Service Bus connection was configured, the Dev Services start an
 * Azure Service Bus Emulator and set the connection string to point to it.
 */
class DeploymentWithDevServicesTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addAsResource(
                            new StringAsset(
                                    """
                                            # We do not set a connection string. This triggers the Dev Services.

                                            quarkus.azure.servicebus.devservices.license-accepted=true
                                            """),
                            "application.properties")
                    .addAsResource("minimal-servicebus-config.json", "servicebus-config.json"));

    @ConfigProperty(name = ServiceBusConfig.CONFIG_KEY_CONNECTION_STRING)
    String connectionString;

    @Test
    void theConnectionStringPointsToTheEmulator() {
        assertThat(connectionString, containsString("UseDevelopmentEmulator=true"));
    }
}
