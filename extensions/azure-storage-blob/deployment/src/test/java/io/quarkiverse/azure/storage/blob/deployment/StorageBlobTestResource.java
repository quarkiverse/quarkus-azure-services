package io.quarkiverse.azure.storage.blob.deployment;

import java.util.Map;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class StorageBlobTestResource implements QuarkusTestResourceLifecycleManager {

    static String accountName = "devstoreaccount1";
    static String accountKey = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";
    static String image = "mcr.microsoft.com/azure-storage/azurite:3.33.0";
    static int port = 10000;
    static String protocol = "http";

    static GenericContainer<?> server = new GenericContainer<>(DockerImageName.parse(image)).withExposedPorts(port);

    @Override
    public Map<String, String> start() {
        server.start();
        return Map.of("quarkus.azure.storage.blob.connection-string", getConnectionString());
    }

    @Override
    public void stop() {
        server.stop();
    }

    public static String getConnectionString() {
        String blobEndpoint = String.format("%s://%s:%s/%s", protocol, server.getHost(), server.getMappedPort(port),
                accountName);
        return String.format("DefaultEndpointsProtocol=%s;AccountName=%s;AccountKey=%s;BlobEndpoint=%s;",
                protocol, accountName, accountKey, blobEndpoint);
    }
}
