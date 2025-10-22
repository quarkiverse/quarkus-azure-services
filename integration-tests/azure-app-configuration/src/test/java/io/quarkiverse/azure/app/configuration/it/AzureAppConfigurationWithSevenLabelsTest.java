package io.quarkiverse.azure.app.configuration.it;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@WithTestResource(AzureAppConfigurationResource.class)
@TestProfile(SevenLabelsConfigurationProfile.class)
class AzureAppConfigurationWithSevenLabelsTest {

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3, 4, 5, 6, 7 })
    void testAppConfigurationSupportsMoreThanFiveLabels(int labelNumber) {
        given()
                .get("/config/{name}", "another.prop.l" + labelNumber)
                .then()
                .statusCode(OK.getStatusCode())
                .body(equalTo("Label " + labelNumber));
    }

    @Test
    void configWithoutLabelIsNotProvided() {
        given()
                .get("/config/{name}", "my.prop")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }
}
