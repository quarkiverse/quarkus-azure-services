package io.quarkiverse.azure.cosmos.deployment;

import java.util.stream.Stream;

import io.quarkiverse.azure.cosmos.runtime.CosmosClientProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;

public class CosmosProcessor {

    static final String FEATURE = "azure-cosmos";

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

    @BuildStep
    void runtimeInitializedClasses(BuildProducer<RuntimeInitializedClassBuildItem> runtimeInitializedClasses) {
        Stream.of(
                "reactor.netty.http.client.HttpClientSecure",
                "reactor.netty.tcp.TcpClientSecure",
                reactor.netty.ReactorNetty.class.getName(),
                "reactor.netty.ConnectionObserver$State",

                com.azure.cosmos.implementation.cpu.CpuMemoryReader.class.getName(),
                com.azure.cosmos.implementation.cpu.CpuMemoryMonitor.class.getName(),
                com.azure.cosmos.implementation.cpu.CpuLoad.class.getName(),
                com.azure.cosmos.implementation.cpu.CpuLoadHistory.class.getName(),
                com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdClientChannelPool.class.getName(),
                "com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdHealthCheckRequest",
                com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestManager.class.getName(),
                "com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdServiceEndpoint$RntbdEndpointMonitoringProvider",
                com.azure.cosmos.implementation.CosmosSchedulers.class.getName())
                .map(RuntimeInitializedClassBuildItem::new)
                .forEach(runtimeInitializedClasses::produce);
    }

    @BuildStep
    void reflectiveClasses(BuildProducer<ReflectiveClassBuildItem> reflectiveClasses) {

        reflectiveClasses.produce(ReflectiveClassBuildItem.builder(
                com.azure.cosmos.implementation.DatabaseAccount.class.getName(),
                com.azure.cosmos.implementation.DocumentCollection.class.getName(),
                "com.azure.cosmos.implementation.ClientSideRequestStatistics$StoreResponseStatistics",
                com.azure.cosmos.models.PartitionKeyDefinition.class.getName(),
                com.azure.cosmos.models.PartitionKind.class.getName(),
                io.netty.channel.epoll.EpollSocketChannel.class.getName()).methods().build());

    }
}
