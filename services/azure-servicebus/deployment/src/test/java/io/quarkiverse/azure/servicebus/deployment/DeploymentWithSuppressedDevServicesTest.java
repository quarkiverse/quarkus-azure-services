package io.quarkiverse.azure.servicebus.deployment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig;
import io.quarkus.test.QuarkusUnitTest;

/**
 * The presence of a connection string (or namespace, not tested here) prevents the Dev Services
 * from launching the Azure Service Bus emulator and targeting the connection string to it.
 */
class DeploymentWithSuppressedDevServicesTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addAsResource(
                            new StringAsset(
                                    """
                                            # We set a connection string. This disables the Dev Services.
                                            quarkus.azure.servicebus.connection-string=some-connection.string

                                            quarkus.azure.servicebus.devservices.license-accepted=true
                                            """),
                            "application.properties")
                    .addAsResource("minimal-servicebus-config.json", "servicebus-config.json"));

    @ConfigProperty(name = ServiceBusConfig.CONFIG_KEY_CONNECTION_STRING)
    String connectionString;

    @Test
    void connectionStringIsNotChanged() {
        assertThat(connectionString, is("some-connection.string"));
    }
}
