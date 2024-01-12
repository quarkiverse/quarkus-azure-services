package io.quarkiverse.azure.app.configuration.it;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class LabelsConfigurationProfile implements QuarkusTestProfile {
    public Map<String, String> getConfigOverrides() {
        return Map.of("quarkus.azure.app.configuration.labels", "prod");
    }
}
