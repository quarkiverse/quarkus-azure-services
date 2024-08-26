package io.quarkiverse.azure.cosmos.deployment;

import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;

import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class CosmosTestResource implements QuarkusTestResourceLifecycleManager {

    private static final Logger LOG = Logger.getLogger(CosmosTestResource.class);
    static String image = "mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator:latest";
    static int port = 8081;
    static String protocol = "https";
    static String cosmosKey = "C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==";

    static GenericContainer<?> server = new GenericContainer<>(DockerImageName.parse(image)).withExposedPorts(port)
            .waitingFor(Wait.forLogMessage(".*Started.*", 12));

    static String certFilePath = "cosmos_emulatorcert.pem";
    static String certAlias = "cosmoscert";
    static String trustStoreFile = "truststore";
    static String storePassword = "changeit";

    @Override
    public Map<String, String> start() {
        server.start();

        try {
            // Fetch the SSL certificate from the emulator
            server.execInContainer("apt", "update");
            server.execInContainer("apt", "install", "curl", "-y");
            server.execInContainer(
                    "curl", "-k", "-o", "/tmp/cosmos_emulatorcert.pem",
                    protocol + "://localhost:" + port + "/_explorer/emulator.pem");
            server.copyFileFromContainer("/tmp/cosmos_emulatorcert.pem", certFilePath);

            LOG.info(server.execInContainer("ls", "-al", "/tmp/cosmos_emulatorcert.pem").toString());
            LOG.info(new File(certFilePath).length());

            // Load the SSL certificate from file
            FileInputStream fis = new FileInputStream(certFilePath);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);
            fis.close();

            // Create a new KeyStore object
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);

            // Add the SSL certificate to the KeyStore as a trusted certificate
            keyStore.setCertificateEntry(certAlias, cert);

            // Set the KeyStore as the default trust store
            System.setProperty("javax.net.ssl.trustStore", trustStoreFile);
            System.setProperty("javax.net.ssl.trustStorePassword", storePassword);
            System.setProperty("javax.net.ssl.trustStoreType", KeyStore.getDefaultType());

            // Save the KeyStore to a file
            FileOutputStream fos = new FileOutputStream(trustStoreFile);
            keyStore.store(fos, storePassword.toCharArray());
            fos.close();
        } catch (IOException | InterruptedException | CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return Map.of("quarkus.azure.cosmos.endpoint",
                String.format("%s://%s:%s", protocol, server.getHost(), server.getMappedPort(port)),
                "quarkus.azure.cosmos.key", cosmosKey);
    }

    @Override
    public void stop() {
        // Delete the certificate file and trust store
        new File(certFilePath).delete();
        new File(trustStoreFile).delete();

        server.stop();
    }
}
