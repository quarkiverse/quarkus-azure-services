package io.quarkiverse.azure.identity.deployment;

import java.util.stream.Stream;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.quarkus.deployment.pkg.builditem.NativeImageRunnerBuildItem;
import io.quarkus.deployment.pkg.steps.NativeOrNativeSourcesBuild;

public class AzureIdentityProcessor {
    @BuildStep(onlyIf = NativeOrNativeSourcesBuild.class)
    void runtimeInitializedClasses(BuildProducer<RuntimeInitializedClassBuildItem> runtimeInitializedClass) {
        Stream.of(
                "com.microsoft.aad.msal4jextensions.persistence.linux.ISecurityLibrary",
                "com.microsoft.aad.msal4jextensions.persistence.mac.ISecurityLibrary")
                .map(RuntimeInitializedClassBuildItem::new)
                .forEach(runtimeInitializedClass::produce);
    }

    @BuildStep(onlyIf = NativeOrNativeSourcesBuild.class)
    public void resources(
            BuildProducer<NativeImageResourceBuildItem> resource,
            NativeImageRunnerBuildItem nativeImageRunnerFactory) {

        // load native resources.
        // https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/aot/azure-aot-graalvm-support/src/main/resources/META-INF/native-image/net.java.dev.jna/jna/resource-config.json
        String dir = "QMETA-INF/services";
        String hotspotLibName = "jdk.vm.ci.hotspot.HotSpotJVMCIBackendFactory";
        String jvmCiServiceLocatorLibName = "jdk.vm.ci.services.JVMCIServiceLocator";
        String hotspotLibPath = dir + "/" + hotspotLibName;
        String jvmCiServiceLocatorLibPath = dir + "/" + jvmCiServiceLocatorLibName;

        resource.produce(new NativeImageResourceBuildItem(hotspotLibPath));
        resource.produce(new NativeImageResourceBuildItem(jvmCiServiceLocatorLibPath));

        String jnaDir = "Qcom/sun/jna/linux-x86-64";
        String jniLibName = "libjnidispatch.so";
        String jniLibPath = jnaDir + "/" + jniLibName;

        resource.produce(new NativeImageResourceBuildItem(jniLibPath));
    }
}
