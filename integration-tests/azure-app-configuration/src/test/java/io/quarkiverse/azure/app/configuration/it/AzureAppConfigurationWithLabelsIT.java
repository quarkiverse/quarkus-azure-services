package io.quarkiverse.azure.app.configuration.it;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusIntegrationTest
@TestProfile(LabelsConfigurationProfile.class)
@DisabledIfSystemProperty(named = "azure.test", matches = "true")
public class AzureAppConfigurationWithLabelsIT extends AzureAppConfigurationWithLabelsTest {

}
