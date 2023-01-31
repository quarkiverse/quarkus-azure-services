package io.quarkiverse.azure.app.configuration.pt;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import com.azure.core.test.TestBase;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class AzureAppConfigurationTest extends TestBase {
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
