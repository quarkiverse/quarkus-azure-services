package io.quarkiverse.azure.app.configuration.it;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@QuarkusTestResource(AzureAppConfigurationResource.class)
@TestProfile(LabelsConfigurationProfile.class)
class AzureAppConfigurationWithLabelsTest {
    @Test
    void azureAppConfigurationSupportsLabels() {
        given()
                .get("/config/{name}", "property.with.label")
                .then()
                .statusCode(OK.getStatusCode())
                .body(equalTo("prodValue"));
    }
}
