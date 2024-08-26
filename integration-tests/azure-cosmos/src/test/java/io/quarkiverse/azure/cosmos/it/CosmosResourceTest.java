package io.quarkiverse.azure.cosmos.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@EnabledIfSystemProperty(named = "azure.test", matches = "true")
class CosmosResourceTest {

    @Test
    void azureCosmos() {
        final String database = "demodb";
        final String container = "democontainer";
        final String item = "{\"id\": \"1\", \"name\": \"dog\"}";
        final String updatedItem = "{\"id\": \"1\", \"name\": \"cat\"}";

        // Create item
        given()
                .when()
                .body(item)
                .header("Content-Type", "application/json")
                .post("/quarkus-azure-cosmos/" + database + "/" + container)
                .then()
                .statusCode(201);

        // Read item
        given()
                .when()
                .get("/quarkus-azure-cosmos/" + database + "/" + container + "/1")
                .then()
                .statusCode(200)
                .body("id", is("1"))
                .body("name", is("dog"));

        // List items
        given()
                .when()
                .get("/quarkus-azure-cosmos/" + database + "/" + container)
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("id", hasItem("1"))
                .body("name", hasItem("dog"));

        // Update item
        given()
                .when()
                .body(updatedItem)
                .header("Content-Type", "application/json")
                .post("/quarkus-azure-cosmos/" + database + "/" + container)
                .then()
                .statusCode(201);

        given()
                .when()
                .get("/quarkus-azure-cosmos/" + database + "/" + container + "/1")
                .then()
                .statusCode(200)
                .body("id", is("1"))
                .body("name", is("cat"));

        given()
                .when()
                .get("/quarkus-azure-cosmos/" + database + "/" + container)
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("id", hasItem("1"))
                .body("name", hasItem("cat"));

        // Delete item
        given()
                .when()
                .delete("/quarkus-azure-cosmos/" + database + "/" + container + "/1")
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/quarkus-azure-cosmos/" + database + "/" + container + "/1")
                .then()
                .statusCode(500);

        given()
                .when()
                .get("/quarkus-azure-cosmos/" + database + "/" + container)
                .then()
                .statusCode(200)
                .body(is("[]"));
    }
}
