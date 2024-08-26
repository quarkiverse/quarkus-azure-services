package io.quarkiverse.azure.app.configuration.it;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(AzureAppConfigurationResource.class)
class AzureAppConfigurationTest {
    @Test
    void azureAppConfiguration() {
        given()
                .get("/config/{name}", "my.prop")
                .then()
                .statusCode(OK.getStatusCode())
                .body(equalTo("1234"));

        given()
                .get("/config/{name}", "another.prop")
                .then()
                .statusCode(OK.getStatusCode())
                .body(equalTo("5678"));
    }
}
