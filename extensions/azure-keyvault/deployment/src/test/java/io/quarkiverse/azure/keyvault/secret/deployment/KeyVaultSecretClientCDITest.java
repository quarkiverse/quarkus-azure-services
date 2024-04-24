package io.quarkiverse.azure.keyvault.secret.deployment;

import com.azure.security.keyvault.secrets.SecretClient;
import io.quarkus.test.QuarkusUnitTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class KeyVaultSecretClientCDITest {

    @Inject
    SecretClient client;

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addAsResource("application.properties"));

    @Test
    public void test() {
        // Application should start with az login.
    }
}
