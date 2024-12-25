package io.quarkiverse.azure.services.disabled.it;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class EventHubsDisabledTest {

    @Test
    public void testGetEventHubProducerClient() {
        given()
                .get("/quarkus-eventhubs-disabled/eventHubProducerClient")
                .then()
                .statusCode(NOT_FOUND.getStatusCode())
                .body(equalTo("The EventHubProducerClient is null because the Azure Event Hubs is disabled"));
    }

    @Test
    public void testGetEventHubConsumerClient() {
        given()
                .get("/quarkus-eventhubs-disabled/eventHubConsumerClient")
                .then()
                .statusCode(NOT_FOUND.getStatusCode())
                .body(equalTo("The EventHubConsumerClient is null because the Azure Event Hubs is disabled"));
    }

    @Test
    public void testGetEventHubProducerAsyncClient() {
        given()
                .get("/quarkus-eventhubs-disabled/eventHubProducerAsyncClient")
                .then()
                .statusCode(NOT_FOUND.getStatusCode())
                .body(equalTo("The EventHubProducerAsyncClient is null because the Azure Event Hubs is disabled"));
    }

    @Test
    public void testGetEventHubConsumerAsyncClient() {
        given()
                .get("/quarkus-eventhubs-disabled/eventHubConsumerAsyncClient")
                .then()
                .statusCode(NOT_FOUND.getStatusCode())
                .body(equalTo("The EventHubConsumerAsyncClient is null because the Azure Event Hubs is disabled"));
    }
}
