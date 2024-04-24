
package io.quarkiverse.azure.keyvault.secret.it;

import java.io.Console;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class KeyVaultSecretApplication implements QuarkusApplication {

    @Inject
    SecretClient secretClient;

    @ConfigProperty(name = "quarkus.azure.keyvault.secret.endpoint")
    String endpoint;

    @Override
    public int run(String... args) throws Exception {

        System.out.println("Keyvault endpoint: " + endpoint);        

        Console con = System.console();

        String secretName = "mySecret" + System.currentTimeMillis();
        System.out.println("Create secret: " + secretName);

        String secretValue = "value" + System.currentTimeMillis();
        System.out.print("Creating a secret called '" + secretName + "' with value '" + secretValue + "' ... ");

        secretClient.setSecret(new KeyVaultSecret(secretName, secretValue));

        System.out.println("done.");
        System.out.println("Forgetting your secret.");

        secretValue = "";
        System.out.println("Your secret's value is '" + secretValue + "'.");

        System.out.println("Retrieving your secret...");

        KeyVaultSecret retrievedSecret = secretClient.getSecret(secretName);

        System.out.println("Your secret's value is '" + retrievedSecret.getValue() + "'.");
        System.out.print("Deleting your secret ... ");

        SyncPoller<DeletedSecret, Void> deletionPoller = secretClient.beginDeleteSecret(secretName);
        deletionPoller.waitForCompletion();

        System.out.println("done.");
        return 0;
    }
}
