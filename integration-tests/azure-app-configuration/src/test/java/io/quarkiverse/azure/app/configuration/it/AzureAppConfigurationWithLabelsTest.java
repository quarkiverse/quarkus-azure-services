package io.quarkiverse.azure.app.configuration.it;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@WithTestResource(AzureAppConfigurationResource.class)
@TestProfile(LabelsConfigurationProfile.class)
class AzureAppConfigurationWithLabelsTest {
    @Test
    void azureAppConfigurationSupportsLabels() {
        given()
                .get("/config/{name}", "another.prop")
                .then()
                .statusCode(OK.getStatusCode())
                .body(equalTo("5678"));
    }

    @Test
    void configWithoutLabelIsNotProvided() {
        given()
                .get("/config/{name}", "my.prop")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }
}
