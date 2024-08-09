package io.quarkiverse.azure.services.disabled.it;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class KeyVaultSecretDisabledTest {

    @Test
    public void testGetSecretClient() {
        given()
                .get("/quarkus-azure-key-vault-secret-disabled/secretClient")
                .then()
                .statusCode(NOT_FOUND.getStatusCode())
                .body(equalTo("The SecretClient is null because the Azure Key Vault secret is disabled"));
    }

    @Test
    public void testGetSecretAsyncClient() {
        given()
                .get("/quarkus-azure-key-vault-secret-disabled/secretAsyncClient")
                .then()
                .statusCode(NOT_FOUND.getStatusCode())
                .body(equalTo("The SecretAsyncClient is null because the Azure Key Vault secret is disabled"));
    }
}
