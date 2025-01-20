package io.quarkiverse.azure.services.disabled.it;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class CosmosDisabledTest {

    @Test
    public void testGetCosmosClient() {
        given()
                .get("/quarkus-azure-cosmos-disabled/cosmosClient")
                .then()
                .statusCode(NOT_FOUND.getStatusCode())
                .body(equalTo("The CosmosClient is null because the Azure Cosmos DB is disabled"));
    }
}
