package io.quarkiverse.azure.servicebus.deployment;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logmanager.Level;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.azure.servicebus.runtime.ServiceBusConfig;
import io.quarkus.test.QuarkusUnitTest;

/**
 * No config file exists, and no custom config file location was specified.
 * The emulator starts with the fallback configuration.
 */
@ExtendWith(ResetDevServicesExtension.class)
class EmulatorStartsWithFallbackConfigTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addAsResource(
                            new StringAsset(
                                    """
                                            quarkus.azure.servicebus.devservices.license-accepted=true
                                            """),
                            "application.properties"))
            // only consider warnings from the Dev Services processor
            .setLogRecordPredicate(logRecord -> logRecord.getLoggerName().equals(ServiceBusDevServicesProcessor.class.getName())
                    && logRecord.getLevel().equals(Level.WARNING))
            // expect a specific log message
            .assertLogRecords(logRecords -> assertThat(logRecords,
                    hasItem(hasProperty("message", containsString("using a fallback configuration")))));

    @ConfigProperty(name = ServiceBusConfig.CONFIG_KEY_CONNECTION_STRING)
    String connectionString;

    @Test
    void theEmulatorStartsWithFallbackConfig() {
        assertThat(connectionString, containsString("UseDevelopmentEmulator=true"));
    }
}
