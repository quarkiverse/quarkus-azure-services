package io.quarkiverse.azureservices.azure.storage.blob.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class StorageBlobResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/azure-storage-blob")
                .then()
                .statusCode(200)
                .body(is("Hello azure-storage-blob"));
    }
}
