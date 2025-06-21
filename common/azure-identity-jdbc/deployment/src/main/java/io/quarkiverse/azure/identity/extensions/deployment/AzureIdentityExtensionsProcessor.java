package io.quarkiverse.azure.identity.extensions.deployment;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;

public class AzureIdentityExtensionsProcessor {

    @BuildStep
    void reflectiveClasses(BuildProducer<ReflectiveClassBuildItem> reflectiveClasses) {

        // Register classes for native-image reflection by name only:
        // * DefaultTokenCredentialProvider is referenced via class.getName(), always on the extension’s classpath.
        // * The JDBC plugin classes (PostgreSQL and MySQL) live in the runtime module but depend on provided-scope driver interfaces
        //   (org.postgresql.plugin.AuthenticationPlugin, com.mysql.cj.protocol.AuthenticationPlugin).
        // * By supplying only the string names, the deployment processor does not load those types or their drivers at build time.
        // * Quarkus emits them into reflect-config.json, and GraalVM native-image silently ignores any entries for classes not present at runtime.
        reflectiveClasses.produce(ReflectiveClassBuildItem.builder(
                com.azure.identity.extensions.implementation.credential.provider.DefaultTokenCredentialProvider.class.getName(),
                "com.azure.identity.extensions.jdbc.postgresql.AzurePostgresqlAuthenticationPlugin",
                "com.azure.identity.extensions.jdbc.mysql.AzureMysqlAuthenticationPlugin").methods().build());
    }
}
