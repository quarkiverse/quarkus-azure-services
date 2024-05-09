package io.quarkiverse.azure.services.disabled.it;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class StorageBlobDisabledTest {

    @Test
    public void testGetBlobServiceClient() {
        given()
                .get("/quarkus-azure-storage-blob-disabled/blobServiceClient")
                .then()
                .statusCode(NOT_FOUND.getStatusCode())
                .body(equalTo("The BlobServiceClient is null because the Azure Storage Blob is disabled"));
    }

    @Test
    public void testGetBlobServiceAsyncClient() {
        given()
                .get("/quarkus-azure-storage-blob-disabled/blobServiceAsyncClient")
                .then()
                .statusCode(NOT_FOUND.getStatusCode())
                .body(equalTo("The BlobServiceAsyncClient is null because the Azure Storage Blob is disabled"));
    }
}
