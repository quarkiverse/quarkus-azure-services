package io.quarkiverse.azure.keyvault.secret.it;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
@EnabledIfSystemProperty(named = "azure.test", matches = "true")
public class KeyVaultSecretConfigResourceIT extends KeyVaultSecretConfigResourceTest {
}
