package io.quarkiverse.azure.keyvault.secret.it;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class KeyVaultSecretClientTest {
    private static final String secretName = "kv" + System.currentTimeMillis();

    @Inject
    SecretClient secretClient;

    @Test
    @Order(1)
    public void testCreateSecret() {
        System.out.print("your secret ... " + secretName);

        String value = "value" + System.currentTimeMillis();
        secretClient.setSecret(secretName, value);

        Assertions.assertEquals(value, secretClient.getSecret(secretName).getValue());
    }

    @Test
    @Order(2)
    public void testDeleteSecret() {
        SyncPoller<DeletedSecret, Void> deletedSecretPoller = secretClient.beginDeleteSecret(secretName);
        // Deleted secret is accessible as soon as polling begins.
        deletedSecretPoller.poll();
        // Secret is being deleted on server.
        deletedSecretPoller.waitForCompletion();

        for (SecretProperties secretProperties : secretClient.listPropertiesOfSecrets()) {
            Assertions.assertNotEquals(secretName, secretProperties.getName());
        }
    }
}
