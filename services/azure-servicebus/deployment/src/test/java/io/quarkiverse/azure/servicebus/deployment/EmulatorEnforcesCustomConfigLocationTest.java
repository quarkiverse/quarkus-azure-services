package io.quarkiverse.azure.servicebus.deployment;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

/**
 * A custom config file location was explicitly configured, but the file does not exist.
 * Dev Services fail to start.
 */
class EmulatorEnforcesCustomConfigLocationTest {

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addAsResource(
                            new StringAsset(
                                    """
                                            quarkus.azure.servicebus.devservices.license-accepted=true

                                            # We explicitly configure a configuration file, but it does not exist.
                                            # This is a severe configuration error, the emulator won't start up.
                                            quarkus.azure.servicebus.devservices.emulator.config-file-path=does-not-exist.json
                                            """),
                            "application.properties"))
            .assertException(exception -> assertThat(exception.getMessage(),
                    containsString("was not found at the location specified with")));

    @Test
    void deploymentFailsDueToMissingConfigFile() {
        throw new AssertionError("Deployment succeeded, even though the config file was missing.");
    }
}
