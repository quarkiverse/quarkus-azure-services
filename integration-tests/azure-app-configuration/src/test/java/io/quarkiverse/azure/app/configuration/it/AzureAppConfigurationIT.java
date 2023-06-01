package io.quarkiverse.azure.app.configuration.it;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
@DisabledIfSystemProperty(named = "azure.test", matches = "true")
public class AzureAppConfigurationIT extends AzureAppConfigurationTest {

}
