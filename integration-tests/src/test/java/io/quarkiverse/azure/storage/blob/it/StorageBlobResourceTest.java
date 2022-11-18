package io.quarkiverse.azure.storage.blob.it;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.startsWith;

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

    @Disabled("https://github.com/quarkiverse/quarkus-azure-services/issues/36")
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
