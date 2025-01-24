package io.quarkiverse.azure.identity.deployment;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;

public class AzureIdentityExtensionsProcessor {

    @BuildStep
    void reflectiveClasses(BuildProducer<ReflectiveClassBuildItem> reflectiveClasses) {

        reflectiveClasses.produce(ReflectiveClassBuildItem.builder(
                com.azure.identity.extensions.jdbc.postgresql.AzurePostgresqlAuthenticationPlugin.class.getName(),
                com.azure.identity.extensions.jdbc.mysql.AzureMysqlAuthenticationPlugin.class.getName()).build());
    }
}
