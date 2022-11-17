package io.quarkiverse.azure.storage.blob.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.startsWith;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StorageBlobResourceTest {

    @Test
    @Order(1)
    void shouldUploadATextfile() {
        given()
                .when().post("/quarkus-azure-storage-blob")
                .then()
                .statusCode(201);
    }

    @Test
    @Order(2)
    void shouldDownloadATextfile() {
        given()
                .when().get("/quarkus-azure-storage-blob")
                .then()
                .statusCode(200)
                .body(startsWith("Hello quarkus-azure-storage-blob"));
    }
}
