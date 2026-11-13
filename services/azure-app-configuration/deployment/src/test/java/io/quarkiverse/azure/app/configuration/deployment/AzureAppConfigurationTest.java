package io.quarkiverse.azure.app.configuration.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;
import io.quarkus.test.common.WithTestResource;
import io.smallrye.config.SmallRyeConfig;

@WithTestResource(AzureAppConfigurationResource.class)
class AzureAppConfigurationTest {
    @RegisterExtension
    static final QuarkusExtensionTest TEST = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    @Inject
    SmallRyeConfig config;

    @Test
    void azureAppConfiguration() {
        assertEquals("1234", config.getConfigValue("my.prop").getRawValue());
        assertEquals("5678", config.getConfigValue("another.prop").getRawValue());
    }
}
