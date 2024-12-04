package io.quarkiverse.azure.eventhubs.deployment;

import io.quarkiverse.azure.eventhubs.runtime.EventhubsClientProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;


public class EventhubsProcessor {

    static final String FEATURE = "azure-eventhubs";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem producer() {
        return new AdditionalBeanBuildItem(EventhubsClientProducer.class);
    }

    @BuildStep
    ExtensionSslNativeSupportBuildItem activateSslNativeSupport() {
        return new ExtensionSslNativeSupportBuildItem(FEATURE);
    }

    @BuildStep
    IndexDependencyBuildItem indexDependency() {
        return new IndexDependencyBuildItem("com.azure", "azure-eventhubs");
    }

}
