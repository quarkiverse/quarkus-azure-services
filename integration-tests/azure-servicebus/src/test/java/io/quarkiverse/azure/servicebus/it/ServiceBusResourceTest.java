package io.quarkiverse.azure.servicebus.it;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@EnabledIfSystemProperty(named = "azure.test", matches = "true")
public class ServiceBusResourceTest {

    private static final String TEST_MESSAGE = "Hello Azure Service Bus!";

    @BeforeEach
    void setUp() {
        // Clear any existing messages before each test
        given()
                .when()
                .delete("/quarkus-azure-servicebus/messages")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"))
                .body("message", equalTo("All messages cleared successfully"));
    }

    @Test
    void testSendAndReceiveMessage() {
        // Send a message
        Map<String, String> messageRequest = Map.of("message", TEST_MESSAGE);

        given()
                .contentType(ContentType.JSON)
                .body(messageRequest)
                .when()
                .post("/quarkus-azure-servicebus/messages")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"))
                .body("message", equalTo("Message sent successfully"));

        // Wait for message to be processed (with timeout)
        await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> {
                    Integer count = given()
                            .when()
                            .get("/quarkus-azure-servicebus/messages/count")
                            .then()
                            .statusCode(200)
                            .extract()
                            .path("count");
                    return count == 1;
                });

        // Verify received messages
        given()
                .when()
                .get("/quarkus-azure-servicebus/messages")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"))
                .body("count", is(1))
                .body("messages", hasSize(1))
                .body("messages[0]", equalTo(TEST_MESSAGE));
    }
}
