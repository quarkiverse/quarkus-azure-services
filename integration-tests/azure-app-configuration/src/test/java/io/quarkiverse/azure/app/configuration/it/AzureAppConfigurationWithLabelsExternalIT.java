package io.quarkiverse.azure.app.configuration.it;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusIntegrationTest
@TestProfile(AzureAppConfigurationWithLabelsExternalIT.TestProfile.class)
@EnabledIfSystemProperty(named = "azure.test", matches = "true")
public class AzureAppConfigurationWithLabelsExternalIT extends AzureAppConfigurationWithLabelsTest {
    public static class TestProfile extends LabelsConfigurationProfile {
        public boolean disableGlobalTestResources() {
            return true;
        }
    }
}
