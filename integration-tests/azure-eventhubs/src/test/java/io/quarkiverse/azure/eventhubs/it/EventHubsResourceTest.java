package io.quarkiverse.azure.eventhubs.it;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@EnabledIfSystemProperty(named = "azure.test", matches = "true")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EventHubsResourceTest {

    @Test
    @Order(1)
    void publishEvents() {
        // Read item
        given()
                .when()
                .get("/quarkus-azure-eventhubs/publishEvents")
                .then()
                .statusCode(204);

    }

    @Test
    @Order(2)
    void consumeEvents() {
        // Read item
        given()
                .when()
                .get("/quarkus-azure-eventhubs/consumeEvents")
                .then()
                .statusCode(204);

    }
}
