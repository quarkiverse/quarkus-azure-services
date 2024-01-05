package io.quarkiverse.azure.app.configuration.it;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

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
