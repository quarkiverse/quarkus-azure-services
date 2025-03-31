package io.quarkiverse.azure.cosmos.deployment;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkiverse.azure.cosmos.deployment.CosmosResource.Item;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class CosmosResourceTest {

    @Test
    void testCreateItem() {
        Item item = new Item("1", "test");
        given()
                .contentType("application/json")
                .body(item)
                .when()
                .post("/cosmos/testdb/testcontainer")
                .then()
                .statusCode(201);
    }

}
