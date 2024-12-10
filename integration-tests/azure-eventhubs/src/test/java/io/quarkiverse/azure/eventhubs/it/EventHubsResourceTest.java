package io.quarkiverse.azure.eventhubs.it;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@EnabledIfSystemProperty(named = "azure.test", matches = "true")
class EventHubsResourceTest {

    @Test
    void eventhubs() {
        // Read item
        given()
                .when()
                .get("/quarkus-azure-eventhubs/publishEvents")
                .then()
                .statusCode(204);

    }
}
