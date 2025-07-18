package io.quarkiverse.azure.servicebus.deployment;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * This extension enables multiple test executions of Azure Service Bus Dev Services in a single test run.
 * Without this extension, Dev Services would only initialize once and run until the test process ends.
 * It works by clearing an internal field of {@link ServiceBusDevServicesProcessor} between test executions.
 * This forces another instance of the Dev Services to launch.
 */
class ResetDevServicesExtension implements BeforeAllCallback {

    private static final Logger log = Logger.getLogger(ResetDevServicesExtension.class);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        log.info("Resetting the dev services");
        ServiceBusDevServicesProcessor.devServices = null;
    }
}
