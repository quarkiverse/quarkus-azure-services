package io.quarkiverse.azure.servicebus.deployment;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.hasProperty;

import java.util.List;
import java.util.logging.LogRecord;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusDevModeTest;

/**
 * If no Service Bus connection was configured, the Dev Services start an
 * Azure Service Bus Emulator and set the connection string to point to it.
 */
public class DeploymentWithDevServicesTest {

    @RegisterExtension
    static final QuarkusDevModeTest TEST = new QuarkusDevModeTest()
            .withApplicationRoot((jar) -> jar
                    .addAsResource(
                            new StringAsset(
                                    """
                                            # We do not set a connection string. This triggers the Dev Services.

                                            quarkus.azure.servicebus.devservices.license-accepted=true
                                            """),
                            "application.properties"))
            .setLogRecordPredicate(
                    logRecord -> logRecord.getLoggerName().equals(ServiceBusDevServicesProcessor.class.getName()));

    @Test
    void theConnectionStringPointsToTheEmulator() {
        List<LogRecord> logRecords = TEST.getLogRecords();
        assertThat(logRecords, hasItem(allOf(
                // As the message was produced with `log.infof()`, `message` contains the non-interpolated format string.
                // We have to check the parameters.
                hasProperty("message", containsString("connection string is")),
                hasProperty("parameters", hasItemInArray(containsString("UseDevelopmentEmulator=true"))))));
    }
}
