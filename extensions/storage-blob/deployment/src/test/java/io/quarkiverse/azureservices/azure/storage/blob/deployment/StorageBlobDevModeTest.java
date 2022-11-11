package io.quarkiverse.azureservices.azure.storage.blob.deployment;

import java.util.function.Supplier;

import org.hamcrest.Matchers;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusDevModeTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.restassured.RestAssured;

@QuarkusTestResource(StorageBlobTestResource.class)
public class StorageBlobDevModeTest {

    @RegisterExtension
    static QuarkusDevModeTest test = new QuarkusDevModeTest()
            .setArchiveProducer(new Supplier<>() {
                @Override
                public JavaArchive get() {
                    return ShrinkWrap.create(JavaArchive.class)
                            .addAsResource(new StringAsset(
                                    "quarkus.azure.storage.blob.connection-string=${quarkus.azure.storage.blob.connection-string}"),
                                    "application.properties")
                            .addClass(StorageBlobResource.class);
                }
            });

    @Test
    public void test() {
        RestAssured.get("/storageblob")
                .then()
                .statusCode(200)
                .body(Matchers.equalTo("samples"));
    }
}
