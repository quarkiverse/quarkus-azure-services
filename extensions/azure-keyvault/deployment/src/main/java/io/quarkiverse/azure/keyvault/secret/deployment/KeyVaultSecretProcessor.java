package io.quarkiverse.azure.keyvault.secret.deployment;

import java.util.Collection;
import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import com.azure.json.JsonSerializable;

import io.quarkiverse.azure.keyvault.secret.runtime.KeyVaultSecretClientProducer;
import io.quarkiverse.azure.keyvault.secret.runtime.config.KeyVaultSecretConfigBuilder;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.*;
import io.quarkus.deployment.builditem.nativeimage.NativeImageProxyDefinitionBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveHierarchyBuildItem;
import io.quarkus.logging.Log;

public class KeyVaultSecretProcessor {

    static final String FEATURE = "azure-keyvault-secret";
    private static final DotName JSON_SERIALIZABLE_DOT_NAME = DotName.createSimple(JsonSerializable.class.getName());

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem producer() {
        return new AdditionalBeanBuildItem(KeyVaultSecretClientProducer.class);
    }

    @BuildStep
    void azureKeyVaultSecretConfigFactory(BuildProducer<RunTimeConfigBuilderBuildItem> runTimeConfigBuilder) {
        runTimeConfigBuilder.produce(new RunTimeConfigBuilderBuildItem(KeyVaultSecretConfigBuilder.class.getName()));
    }

    @BuildStep
    ExtensionSslNativeSupportBuildItem activateSslNativeSupport() {
        return new ExtensionSslNativeSupportBuildItem(FEATURE);
    }

    @BuildStep
    IndexDependencyBuildItem indexDependency() {
        return new IndexDependencyBuildItem("com.azure", "azure-security-keyvault-secrets");
    }

    @BuildStep
    void reflectiveClasses(CombinedIndexBuildItem combinedIndexBuildItem,
            BuildProducer<ReflectiveClassBuildItem> reflectiveClasses) {

        // see https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-secrets/src/main/resources/META-INF/native-image/com.azure/azure-security-keyvault-secrets/reflect-config.json
        reflectiveClasses.produce(
                ReflectiveClassBuildItem
                        .builder("com.azure.security.keyvault.secrets.SecretAsyncClient",
                                "com.azure.security.keyvault.secrets.SecretClient",
                                "com.azure.security.keyvault.secrets.SecretClientBuilder",
                                "com.azure.security.keyvault.secrets.SecretServiceVersion",
                                "com.azure.security.keyvault.secrets.implementation.DeletedSecretPage",
                                "com.azure.security.keyvault.secrets.implementation.KeyVaultCredentialPolicy",
                                "com.azure.security.keyvault.secrets.implementation.KeyVaultErrorCodeStrings",
                                "com.azure.security.keyvault.secrets.implementation.SecretBackup",
                                "com.azure.security.keyvault.secrets.implementation.SecretClientImpl",
                                "com.azure.security.keyvault.secrets.implementation.SecretPropertiesPage",
                                "com.azure.security.keyvault.secrets.implementation.SecretRequestAttributes",
                                "com.azure.security.keyvault.secrets.implementation.SecretRequestParameters",
                                "com.azure.security.keyvault.secrets.implementation.SecretRestoreRequestParameters",
                                "com.azure.security.keyvault.secrets.implementation.models.DeletedSecretBundle",
                                "com.azure.security.keyvault.secrets.implementation.models.DeletionRecoveryLevel",
                                "com.azure.security.keyvault.secrets.implementation.models.KeyVaultErrorException",
                                "com.azure.security.keyvault.secrets.implementation.models.SecretsModelsUtils")
                        .methods().build());
    }

    @BuildStep
    void reflectiveHierarchyClass(CombinedIndexBuildItem combinedIndexBuildItem,
            BuildProducer<ReflectiveHierarchyBuildItem> reflectiveHierarchyClass) {

        final var fullIndex = combinedIndexBuildItem.getIndex();
        Collection<ClassInfo> jsonSerializableImpls = fullIndex.getAllKnownImplementors(JSON_SERIALIZABLE_DOT_NAME);
        jsonSerializableImpls
                .stream()
                .map(c -> c.name().toString())
                .filter(s -> s.startsWith("com.azure.security.keyvault.secrets.implementation.models."))
                .forEach(e -> {
                    if (Log.isDebugEnabled()) {
                        Log.debugv("Add class to reflectiveHierarchyClass: " + e);
                    }
                    Type jandexType = Type.create(DotName.createSimple(e), Type.Kind.CLASS);
                    reflectiveHierarchyClass.produce(ReflectiveHierarchyBuildItem.builder(jandexType)
                            .source(getClass().getSimpleName() + " > " + jandexType.name().toString())
                            .build());
                });

    }

    @BuildStep
    void proxyDefinitions(
            CombinedIndexBuildItem combinedIndex,
            BuildProducer<NativeImageProxyDefinitionBuildItem> proxyDefinitions) {

        proxyDefinitions.produce(new NativeImageProxyDefinitionBuildItem(
                List.of("com.azure.security.keyvault.secrets.implementation.SecretClientImpl$SecretClientService")));
    }
}
