package io.quarkiverse.azure.servicebus.deployment;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;

import java.util.List;
import java.util.logging.LogRecord;

import org.jboss.logmanager.Level;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusDevModeTest;

/**
 * No config file exists, and no custom config file location was specified.
 * The emulator starts with the fallback configuration.
 */
public class EmulatorStartsWithFallbackConfigTest {

    @RegisterExtension
    static final QuarkusDevModeTest TEST = new QuarkusDevModeTest()
            .withApplicationRoot((jar) -> jar
                    .addAsResource(
                            new StringAsset(
                                    """
                                            quarkus.azure.servicebus.devservices.license-accepted=true
                                            """),
                            "application.properties"))
            // only consider warnings from the Dev Services processor
            .setLogRecordPredicate(logRecord -> logRecord.getLoggerName().equals(ServiceBusDevServicesProcessor.class.getName())
                    && logRecord.getLevel().equals(Level.WARNING));

    @Test
    void theEmulatorStartsWithFallbackConfig() {
        List<LogRecord> logRecords = TEST.getLogRecords();
        assertThat(logRecords, hasItem(hasProperty("message", containsString("using a fallback configuration"))));
    }
}
