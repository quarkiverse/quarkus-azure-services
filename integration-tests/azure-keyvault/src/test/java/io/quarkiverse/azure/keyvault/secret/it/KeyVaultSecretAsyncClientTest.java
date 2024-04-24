package io.quarkiverse.azure.keyvault.secret.it;

import java.lang.Thread;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import com.azure.security.keyvault.secrets.SecretAsyncClient;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class KeyVaultSecretAsyncClientTest {
    private static final String secretName = "kv" + System.currentTimeMillis();

    @Inject
    SecretAsyncClient secretAsyncClient;

    @Test
    @Order(1)
    public void testCreateSecret() throws InterruptedException {
        System.out.print("your secret ... " + secretName);

        String value = "value" + System.currentTimeMillis();
        secretAsyncClient.setSecret(secretName, value)
                .subscribe(secret -> Assertions.assertEquals(value, secret.getValue()));

        Thread.currentThread().sleep(2000);
    }

    @Test
    @Order(2)
    public void testDeleteSecret() throws InterruptedException {
        secretAsyncClient.beginDeleteSecret(secretName)
                .subscribe(pollResponse -> {
                    Assertions.assertEquals(secretName, pollResponse.getValue().getName());
                });
        Thread.currentThread().sleep(2000);

        secretAsyncClient.listPropertiesOfSecrets()
                .flatMap(secretProperties -> secretAsyncClient.getSecret(secretProperties.getName(),
                        secretProperties.getVersion()))
                .subscribe(secretResponse -> Assertions.assertNotEquals(secretName, secretResponse.getName()));
    }
}
