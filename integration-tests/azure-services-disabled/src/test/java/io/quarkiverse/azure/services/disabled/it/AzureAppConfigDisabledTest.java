package io.quarkiverse.azure.services.disabled.it;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class AzureAppConfigDisabledTest {

    @Test
    public void testGetValue() {
        given()
                .get("/quarkus-azure-app-config-disabled/{name}", "my.prop")
                .then()
                .statusCode(NOT_FOUND.getStatusCode())
                .body(equalTo("The value is null because the Azure App Configuration is disabled"));

        given()
                .get("/quarkus-azure-app-config-disabled/{name}", "another.prop")
                .then()
                .statusCode(NOT_FOUND.getStatusCode())
                .body(equalTo("The value is null because the Azure App Configuration is disabled"));
    }
}
