package io.quarkiverse.azure.keyvault.secret.it;

import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
@EnabledIfSystemProperty(named = "azure.test", matches = "true")
class KeyVaultSecretConfigResourceTest {

    @Test
    void getExistingSecret() {
        RestAssured.when().get("/keyvaultConfig/getSecret1").then().body(is("mysecret"));
    }

    @Test
    void getNotExistingSecret() {
        RestAssured.when().get("/keyvaultConfig/getSecret2").then().body(is(null));
    }
}
