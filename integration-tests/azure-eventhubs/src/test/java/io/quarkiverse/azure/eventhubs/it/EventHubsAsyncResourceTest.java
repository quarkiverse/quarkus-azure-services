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
class EventHubsAsyncResourceTest {

    @Order(1)
    @Test
    void sendEvents() {
        // publish events
        given()
                .when()
                .get("/quarkus-azure-eventhubs-async/sendEvents")
                .then()
                .statusCode(204);
    }

    @Order(2)
    @Test
    void receiveEvents() {
        // Read item
        given()
                .when()
                .get("/quarkus-azure-eventhubs-async/receiveEvents")
                .then()
                .statusCode(204);
    }
}
