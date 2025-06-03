package io.quarkiverse.azure.servicebus.deployment;

import java.util.stream.Stream;

import io.quarkiverse.azure.servicebus.runtime.ServiceBusBuildTimeConfig;
import io.quarkiverse.azure.servicebus.runtime.ServiceBusClientProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;

public class ServiceBusProcessor {

    static final String FEATURE = "azure-servicebus";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem producer(ServiceBusBuildTimeConfig config) {
        if (config.enabled()) {
            return new AdditionalBeanBuildItem(ServiceBusClientProducer.class);
        }
        return null;
    }

    @BuildStep
    ExtensionSslNativeSupportBuildItem activateSslNativeSupport() {
        return new ExtensionSslNativeSupportBuildItem(FEATURE);
    }

    @BuildStep
    void runtimeInitializedClasses(BuildProducer<RuntimeInitializedClassBuildItem> runtimeInitializedClasses) {
        Stream.of(
                "com.microsoft.azure.proton.transport.proxy.impl.DigestProxyChallengeProcessorImpl",
                "com.microsoft.azure.proton.transport.ws.impl.Utils")
                .map(RuntimeInitializedClassBuildItem::new)
                .forEach(runtimeInitializedClasses::produce);
    }
}
