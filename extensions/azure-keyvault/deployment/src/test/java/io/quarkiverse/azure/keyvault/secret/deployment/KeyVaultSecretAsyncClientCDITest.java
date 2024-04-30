package io.quarkiverse.azure.keyvault.secret.deployment;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.azure.security.keyvault.secrets.SecretAsyncClient;

import io.quarkus.test.QuarkusUnitTest;

public class KeyVaultSecretAsyncClientCDITest {

    @Inject
    SecretAsyncClient asyncClient;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addAsResource("application.properties"));

    @Test
    public void test() {
        // Application should start with az login.
    }
}
