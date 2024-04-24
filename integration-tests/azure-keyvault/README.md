# Azure Key Vault sample

This is a Quarkus CLI sample application using the Quarkus Key Vault extension to create/delete secret from Azure Key Vault.

## Prerequisites

To successfully run this sample, you need:

* JDK 11+ installed with JAVA_HOME configured appropriately
* Apache Maven 3.8.6+
* Azure CLI and Azure subscription if the specific Azure services are required
* Docker if you want to build the app as a native executable

You also need to make sure the right version of dependencies are installed.

### Use development iteration version

By default, the sample depends on the development iteration version, which is `999-SNAPSHOT`. To install the development
iteration version, you need to build it locally.

```
# Switch to the root directory of Quarkus Azure services extensions.
# For example, if you are in the directory of quarkus-azure-services/integration-tests/azure-keyvault
cd ../..

# Install all Quarkus Azure services extensions locally.
mvn clean install -DskipTests

# Switch back to the directory of integration-tests/azure-keyvault
cd integration-tests/azure-keyvault
```

### Use release version

If you want to use the release version, you need to update the version of dependencies in the `pom.xml` file.

First, you need to find out the latest release version of the Quarkus Azure services extensions
from [releases](https://github.com/quarkiverse/quarkus-azure-services/releases), for example, `1.0.0`.

Then, update the version of dependencies in the `pom.xml` file, for example:

```xml

<parent>
    <groupId>io.quarkiverse.azureservices</groupId>
    <artifactId>quarkus-azure-services-parent</artifactId>
    <version>1.0.0</version>
    <relativePath></relativePath>
</parent>
```

## Preparing the Azure services

You need to create an Azure Key Vault before runnint the sample application.

### Logging into Azure

Log into Azure and create a resource group for hosting the Key Vault to be created.

```
az login

RESOURCE_GROUP_NAME=<resource-group-name>
az group create \
    --name ${RESOURCE_GROUP_NAME} \
    --location eastus
```

### Creating Azure Key Vault

Run the following commands to create an Azure Key Vault, set permission and export its connection string as an environment
variable.

```
KEY_VAULT_NAME=<unique-key-vault-name>
az keyvault create --name ${KEY_VAULT_NAME} \
    --resource-group ${RESOURCE_GROUP_NAME} \
    --location eastus

az ad signed-in-user show --query id -o tsv \
    | az keyvault set-policy \
    --name ${KEY_VAULT_NAME} \
    --object-id @- \
    --secret-permissions all

export QUARKUS_AZURE_KEYVAULT_SECRET_ENDPOINT=$(az keyvault show --name ${KEY_VAULT_NAME}\
    --resource-group ${RESOURCE_GROUP_NAME}\
    --query properties.vaultUri -otsv)
echo "The value of 'quarkus.azure.keyvault.secret.endpoint' is: ${QUARKUS_AZURE_KEYVAULT_SECRET_ENDPOINT}"
```

The value of environment variable `QUARKUS_AZURE_KEYVAULT_SECRET_ENDPOINT` will be fed into config
property `quarkus.azure.keyvault.secret.endpoint` of `azure-keyvault` extension in order to set up the
connection to the Azure Key Vault.

You can also manually copy the output of the variable `quarkus.azure.keyvault.secret.endpoint` and then
update [application.properties](src/main/resources/application.properties) by uncommenting the
same property and setting copied value.

## Running the sample

You have different choices to run the sample.

### Running the sample in development mode

First, you can launch the sample in `dev` mode.

```
mvn quarkus:dev
```

### Running and test the sample in JVM mode

You can also run the sample in JVM mode. Make sure you have
followed [Preparing the Azure services](#preparing-the-azure-services) to create the required Azure services.

```
# Build the package.
mvn package

# Run the generated jar file.
java -jar ./target/quarkus-app/quarkus-run.jar
```

This is a Quarkus CLI appliation. You will find the output of Key Vault endpoint and secret name. 
When you are asked to input a secret value, provide a secret vault and press Enter.

The output looks similar to the following content.

```text
java -jar ./target/quarkus-app/quarkus-run.jar
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2024-04-24 03:04:03,835 INFO  [io.quarkus] (main) quarkus-azure-integration-test-keyvault-secret 999-SNAPSHOT on JVM (powered by Quarkus 3.6.5) started in 1.853s. Listening on: http://0.0.0.0:8080
2024-04-24 03:04:03,845 INFO  [io.quarkus] (main) Profile prod activated. 
2024-04-24 03:04:03,845 INFO  [io.quarkus] (main) Installed features: [azure-keyvault-secret, cdi, smallrye-context-propagation, vertx]
Keyvault endpoint: https://kvquarkusazurekv0423.vault.azure.net/
Create secret: mySecret1713927844606
Creating a secret called 'mySecret1713927844606' with value 'value1713927844608' ... 
2024-04-24 03:04:06,031 INFO  [com.azu.ide.ChainedTokenCredential] (main) Azure Identity => Attempted credential EnvironmentCredential is unavailable.
2024-04-24 03:04:06,032 INFO  [com.azu.ide.ChainedTokenCredential] (main) Azure Identity => Attempted credential WorkloadIdentityCredential is unavailable.
2024-04-24 03:04:06,242 WARN  [com.mic.aad.msa.ConfidentialClientApplication] (ForkJoinPool.commonPool-worker-1) [Correlation ID: efba4900-f323-4f76-88b4-ffb520075923] Execution of class com.microsoft.aad.msal4j.AcquireTokenByClientCredentialSupplier failed: java.util.concurrent.ExecutionException: com.azure.identity.CredentialUnavailableException: ManagedIdentityCredential authentication unavailable. Connection to IMDS endpoint cannot be established.
2024-04-24 03:04:06,242 INFO  [com.azu.ide.ChainedTokenCredential] (main) Azure Identity => Attempted credential ManagedIdentityCredential is unavailable.
2024-04-24 03:04:06,253 INFO  [com.azu.ide.ChainedTokenCredential] (main) Azure Identity => Attempted credential SharedTokenCacheCredential is unavailable.
2024-04-24 03:04:06,311 INFO  [com.azu.ide.ChainedTokenCredential] (main) Azure Identity => Attempted credential IntelliJCredential is unavailable.
2024-04-24 03:04:06,813 INFO  [com.azu.ide.AzureCliCredential] (main) Azure Identity => getToken() result for scopes [https://vault.azure.net/.default]: SUCCESS
2024-04-24 03:04:06,814 INFO  [com.azu.cor.imp.AccessTokenCache] (main) {"az.sdk.message":"Acquired a new access token."}
done.
Forgetting your secret.
Your secret's value is ''.
Retrieving your secret...
Your secret's value is 'value1713927844608'.
Deleting your secret ... 
done.
2024-04-24 03:04:27,914 INFO  [io.quarkus] (main) quarkus-azure-integration-test-keyvault-secret stopped in 0.065s
```

### Running and test the sample as a native executable

You can even run the sample as a native executable. Make sure you have installed Docker and
followed [Preparing the Azure services](#preparing-the-azure-services) to create the required Azure services.

```
# Build the native executable using the Docker.
mvn package -Dnative

# Run the native executable.
version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
./target/quarkus-azure-integration-test-keyvault-secret-${version}-runner
```

You will find the output of Key Vault endpoint and secret name. 
When you are asked to input a secret value, provide a secret vault and press Enter.

The output looks similar to the following content.

```text
./target/quarkus-azure-integration-test-keyvault-secret-${version}-runner
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2024-04-24 03:10:28,110 INFO  [io.quarkus] (main) quarkus-azure-integration-test-keyvault-secret 999-SNAPSHOT native (powered by Quarkus 3.6.5) started in 1.064s. Listening on: http://0.0.0.0:8080
2024-04-24 03:10:28,124 INFO  [io.quarkus] (main) Profile prod activated. 
2024-04-24 03:10:28,124 INFO  [io.quarkus] (main) Installed features: [azure-keyvault-secret, cdi, smallrye-context-propagation, vertx]
Keyvault endpoint: https://kvquarkusazurekv0423.vault.azure.net/
Create secret: mySecret1713928228223
Creating a secret called 'mySecret1713928228223' with value 'value1713928228223' ... 
2024-04-24 03:10:29,383 INFO  [com.azu.ide.ChainedTokenCredential] (main) Azure Identity => Attempted credential EnvironmentCredential is unavailable.
2024-04-24 03:10:29,384 INFO  [com.azu.ide.ChainedTokenCredential] (main) Azure Identity => Attempted credential WorkloadIdentityCredential is unavailable.
2024-04-24 03:10:29,490 WARN  [com.mic.aad.msa.ConfidentialClientApplication] (ForkJoinPool.commonPool-worker-1) [Correlation ID: b26f3df6-0aa0-4571-b4ff-266b335cf158] Execution of class com.microsoft.aad.msal4j.AcquireTokenByClientCredentialSupplier failed: java.util.concurrent.ExecutionException: com.azure.identity.CredentialUnavailableException: ManagedIdentityCredential authentication unavailable. Connection to IMDS endpoint cannot be established.
2024-04-24 03:10:29,491 INFO  [com.azu.ide.ChainedTokenCredential] (main) Azure Identity => Attempted credential ManagedIdentityCredential is unavailable.
2024-04-24 03:10:29,492 INFO  [com.azu.ide.ChainedTokenCredential] (main) Azure Identity => Attempted credential SharedTokenCacheCredential is unavailable.
2024-04-24 03:10:29,519 INFO  [com.azu.ide.ChainedTokenCredential] (main) Azure Identity => Attempted credential IntelliJCredential is unavailable.
2024-04-24 03:10:30,186 INFO  [com.azu.ide.AzureCliCredential] (main) Azure Identity => getToken() result for scopes [https://vault.azure.net/.default]: SUCCESS
2024-04-24 03:10:30,186 INFO  [com.azu.cor.imp.AccessTokenCache] (main) {"az.sdk.message":"Acquired a new access token."}
done.
Forgetting your secret.
Your secret's value is ''.
Retrieving your secret...
Your secret's value is 'value1713928228223'.
Deleting your secret ... 
done.
2024-04-24 03:10:36,270 INFO  [io.quarkus] (main) quarkus-azure-integration-test-keyvault-secret stopped in 0.009s
```

## Cleaning up Azure resources

Run the following command to clean up the Azure resources if you created before:

```
az group delete \
    --name ${RESOURCE_GROUP_NAME} \
    --yes --no-wait
```