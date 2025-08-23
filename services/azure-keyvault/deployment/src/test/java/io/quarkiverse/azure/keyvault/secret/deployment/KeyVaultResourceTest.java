package io.quarkiverse.azure.keyvault.secret.deployment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkiverse.azure.keyvault.secret.deployment.KeyVaultResource.SecretItem;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class KeyVaultResourceTest {

    @Test
    void testCreateSecretItem() {
        SecretItem item = new SecretItem("secret-name", "test-value");
        String location = given()
                .contentType("application/json")
                .body(item)
                .when()
                .post("/key-vault-secrets")
                .then()
                .statusCode(201)
                .extract()
                .response()
                .header("Location");

        String relativePath = location.replaceFirst("https://[^/]+", "");
        given()
                .contentType("application/json")
                .when()
                .get(relativePath)
                .then()
                .statusCode(200)
                .body("name", equalTo("secret-name"))
                .body("value", equalTo("test-value"));
    }

    @Test
    void testInjectedSecretIsAsExpected() {
        given()
                .contentType("application/json")
                .when()
                .get("/key-vault-secrets/injected")
                .then()
                .statusCode(200)
                .body("name", equalTo("secret1"))
                .body("value", equalTo("value1"));
    }

}
