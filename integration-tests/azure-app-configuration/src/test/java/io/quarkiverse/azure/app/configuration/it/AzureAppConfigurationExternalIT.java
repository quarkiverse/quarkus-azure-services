package io.quarkiverse.azure.app.configuration.it;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusIntegrationTest
@TestProfile(AzureAppConfigurationExternalIT.TestProfile.class)
@EnabledIfSystemProperty(named = "azure.test", matches = "true")
public class AzureAppConfigurationExternalIT extends AzureAppConfigurationTest {
    public static class TestProfile implements QuarkusTestProfile {
        public boolean disableGlobalTestResources() {
            return true;
        }
    }
}
