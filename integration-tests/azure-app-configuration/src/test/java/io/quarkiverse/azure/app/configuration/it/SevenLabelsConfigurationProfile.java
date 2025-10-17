package io.quarkiverse.azure.app.configuration.it;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class SevenLabelsConfigurationProfile implements QuarkusTestProfile {
    public Map<String, String> getConfigOverrides() {
        return Map.of("quarkus.azure.app.configuration.labels", "l1,l2,l3,l4,l5,l6,l7");
    }
}
