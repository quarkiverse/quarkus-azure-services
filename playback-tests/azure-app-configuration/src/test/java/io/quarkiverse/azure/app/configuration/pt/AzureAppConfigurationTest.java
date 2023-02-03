package io.quarkiverse.azure.app.configuration.pt;

import com.azure.core.test.TestBase;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.equalTo;

/**
 * When executed in RECORD mode, the entire Azure environment needs to be really created. That means creating the resource group, the App Configuration and the key/values used in this test.
 * 
 * az appconfig kv set --name $APP_CONFIG_NAME --yes --key my.prop --value "1234"
 * az appconfig kv set --name $APP_CONFIG_NAME --yes --key another.prop --value "5678"
 */
@QuarkusTest
class AzureAppConfigurationTest extends TestBase {
    @Test
    void azureAppConfiguration() {
        given()
                .get("/config/{name}", "my.prop")
                .then()
                .statusCode(OK.getStatusCode())
                .body(equalTo("1234"));

        given()
                .get("/config/{name}", "another.prop")
                .then()
                .statusCode(OK.getStatusCode())
                .body(equalTo("5678"));
    }
}
