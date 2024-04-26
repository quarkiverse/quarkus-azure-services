package io.quarkiverse.azure.keyvault.secret.it;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import org.jboss.logging.Logger;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.SecretProperties;

@Path("/keyvault")
public class KeyVaultSecretResource {
    private static final Logger LOG = Logger.getLogger(KeyVaultSecretResource.class);

    public final static String TEXT = "Quarkus Azure Key Vault Extension is awsome";
    private static final String SYNC_PARAM = "synkv" + System.currentTimeMillis();
    private static final String ASYNC_PARAM = "asynckv" + System.currentTimeMillis();

    @Inject
    SecretClient secretClient;

    @Inject
    SecretAsyncClient secretAsyncClient;

    @GET
    @Path("sync")
    @Produces(TEXT_PLAIN)
    public String testSync() {
        LOG.info("Testing SecretClient by creating secret: " + SYNC_PARAM);
        //Put parameter
        secretClient.setSecret(SYNC_PARAM, TEXT);
        //Get parameter
        return secretClient.getSecret(SYNC_PARAM).getValue();
    }

    @GET
    @Path("list")
    @Produces(TEXT_PLAIN)
    public String testSyncList() {
        LOG.info("Testing SecretClient by listing secrets: ");

        //Get secrets
        return secretClient.listPropertiesOfSecrets()
                .stream()
                .map(SecretProperties::getName)
                .collect(Collectors.joining(","));
    }

    @GET
    @Path("async")
    @Produces(TEXT_PLAIN)
    public CompletableFuture<String> testAsync() {
        LOG.info("Testing SecretAsyncClient by creating secret: " + ASYNC_PARAM);

        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        secretAsyncClient.setSecret(ASYNC_PARAM, TEXT)
                .subscribe(secret -> completableFuture.complete(secret.getValue()));

        return completableFuture.toCompletableFuture();
    }
}
