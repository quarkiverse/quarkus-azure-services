package io.quarkiverse.azure.services.together.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@EnabledIfSystemProperty(named = "azure.test", matches = "true")
public class KeyVaultSecretResourceTest {

    @Test
    public void testKeyVaultSecret() {
        final String value = "mysecret";

        // Create secret and read it back using SecretClient
        given()
                .when().get("/quarkus-services-azure-key-vault/getSecretBySecretClient")
                .then()
                .statusCode(200)
                .body(is(value));

        // Read secret value from config property
        given()
                .when().get("/quarkus-services-azure-key-vault/getSecretByConfigProperty")
                .then()
                .statusCode(200)
                .body(is(value));
    }
}
