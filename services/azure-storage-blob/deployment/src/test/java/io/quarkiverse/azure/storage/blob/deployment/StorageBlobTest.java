package io.quarkiverse.azure.storage.blob.deployment;

import org.hamcrest.Matchers;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;
import io.quarkus.test.common.WithTestResource;
import io.restassured.RestAssured;

@WithTestResource(StorageBlobTestResource.class)
public class StorageBlobTest {

    @RegisterExtension
    static QuarkusExtensionTest test = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(StorageBlobResource.class));

    @Test
    public void test() {
        RestAssured.get("/storageblob")
                .then()
                .statusCode(200)
                .body(Matchers.equalTo("samples"));
    }
}
