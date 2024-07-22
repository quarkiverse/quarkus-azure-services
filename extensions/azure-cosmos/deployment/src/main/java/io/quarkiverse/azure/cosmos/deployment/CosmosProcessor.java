package io.quarkiverse.azure.cosmos.deployment;

import org.jboss.jandex.DotName;

import com.azure.json.JsonSerializable;

import io.quarkiverse.azure.cosmos.runtime.CosmosClientProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;

public class CosmosProcessor {

    static final String FEATURE = "azure-cosmos";
    private static final DotName JSON_SERIALIZABLE_DOT_NAME = DotName.createSimple(JsonSerializable.class.getName());

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem producer() {
        return new AdditionalBeanBuildItem(CosmosClientProducer.class);
    }

    @BuildStep
    ExtensionSslNativeSupportBuildItem activateSslNativeSupport() {
        return new ExtensionSslNativeSupportBuildItem(FEATURE);
    }

    @BuildStep
    IndexDependencyBuildItem indexDependency() {
        return new IndexDependencyBuildItem("com.azure", "azure-cosmos");
    }

}
