# Quarkus extensions for Azure services sample

This is a sample about using multiple Quarkus extensions for Azure services together.

> **NOTE:** Currently, the sample only demonstrates the integration of Quarkus Azure storage blob extension with Quarkus Azure Key Vault extension. More Quarkus Azure services extensions will be added in the future.

## Prerequisites

To successfully run this sample, you need:

* JDK 17+ installed with JAVA_HOME configured appropriately
* Apache Maven 3.8.6+
* Azure CLI and Azure subscription
* Docker

You also need to clone the repository and switch to the directory of the sample.

```
git clone https://github.com/quarkiverse/quarkus-azure-services.git
cd quarkus-azure-services/integration-tests/azure-services-together
```

### Use development iteration version

By default, the sample depends on the development iteration version, which is `999-SNAPSHOT`. To install the development
iteration version, you need to build it locally.

```
mvn clean install -DskipTests --file ../../pom.xml
```

### Use release version

If you want to use the release version, you need to update the version of dependencies in the `pom.xml` file.

First, you need to find out the latest release version of the Quarkus Azure services extensions
from [releases](https://github.com/quarkiverse/quarkus-azure-services/releases), for example, `1.2.0`.

Then, update the version of dependencies in the `pom.xml` file, for example:

```xml
<parent>
    <groupId>io.quarkiverse.azureservices</groupId>
    <artifactId>quarkus-azure-services-parent</artifactId>
    <version>1.2.0</version>
    <relativePath></relativePath>
</parent>
```

## Preparing the Azure services

You need to create an Azure Blob Storage and an Azure Key Vault before running the sample application.

### Logging into Azure

Log into Azure and create a resource group for hosting the Azure storage blob to be created.

```
az login

RESOURCE_GROUP_NAME=<resource-group-name>
az group create \
    --name ${RESOURCE_GROUP_NAME} \
    --location eastus
```

### Creating Azure Storage Account

Run the following commands to create an Azure Storage Account and retrieve its connection string. 

```
STORAGE_ACCOUNT_NAME=<unique-storage-account-name>
az storage account create \
    --name ${STORAGE_ACCOUNT_NAME} \
    --resource-group ${RESOURCE_GROUP_NAME} \
    --location eastus \
    --sku Standard_LRS \
    --kind StorageV2

AZURE_STORAGE_BLOB_CONNECTION_STRING=$(az storage account show-connection-string \
    --resource-group ${RESOURCE_GROUP_NAME} \
    --name ${STORAGE_ACCOUNT_NAME} \
    --query connectionString -o tsv)
```

You have two options to authenticate to Azure Storage Blob, either with Microsoft Entra ID or with connection string. 
In this sample, you use the 2nd option where the connection string of Azure Storage Blob is stored as a secret in the Azure Key Vault, and it's retrieved as a configuration property using the Azure Key Vault extension later.

### Creating Azure Key Vault

Run the following commands to create an Azure Key Vault.

```
KEY_VAULT_NAME=<unique-key-vault-name>
az keyvault create --name ${KEY_VAULT_NAME} \
    --resource-group ${RESOURCE_GROUP_NAME} \
    --location eastus \
    --enable-rbac-authorization false
```

Add secret `secret-azure-storage-blob-conn-string` with value of the connection string of Azure Storage Blob `AZURE_STORAGE_BLOB_CONNECTION_STRING`.

```
az keyvault secret set \
    --vault-name ${KEY_VAULT_NAME} \
    --name secret-azure-storage-blob-conn-string \
    --value "$AZURE_STORAGE_BLOB_CONNECTION_STRING"
```

Add secret `secret1` with value `mysecret`.

```
az keyvault secret set \
    --vault-name ${KEY_VAULT_NAME} \
    --name secret1 \
    --value mysecret
```

### Exporting environment variables

Run the following commands to export the necessary environment variables to sucessfully run the sample.

```
# Enable the Quarkus Azure Key Vault and Azure Storage Blob extensions
export QUARKUS_AZURE_KEYVAULT_SECRET_ENABLED=true
export QUARKUS_AZURE_STORAGE_BLOB_ENABLED=true

# Export the Azure Key Vault endpoint
export QUARKUS_AZURE_KEYVAULT_SECRET_ENDPOINT=$(az keyvault show --name ${KEY_VAULT_NAME}\
    --resource-group ${RESOURCE_GROUP_NAME}\
    --query properties.vaultUri -otsv)

# Export the Azure Storage Blob connection string which is stored as a secret in the Azure Key Vault
export QUARKUS_AZURE_STORAGE_BLOB_CONNECTION_STRING=\${kv//secret-azure-storage-blob-conn-string}
```

These environment variables will be used to configure the Quarkus Azure Key Vault and Azure Storage Blob extensions, for the following configurations:

* `quarkus.azure.keyvault.secret.enabled` - Enable / disable the Azure Key Vault extension.
* `quarkus.azure.storage.blob.enabled` - Enable / disable the Azure Storage Blob extension.
* `quarkus.azure.keyvault.secret.endpoint` - The endpoint of the Azure Key Vault.
* `quarkus.azure.storage.blob.connection-string` - The connection string of the Azure Storage Blob.

## Running the sample

You have different choices to run the sample. For each choice, follow [Testing the sample](#testing-the-sample) to test the sample and try the next choice.

### Running the sample in development mode

First, launch the sample in `dev` mode.

```
mvn quarkus:dev
```

### Running and test the sample in JVM mode

Next, run the sample in JVM mode. 

```
# Build the package.
mvn package

# Run the generated jar file.
java -jar ./target/quarkus-app/quarkus-run.jar
```

### Running and test the sample as a native executable

Finally, run the sample as a native executable.

```
# Build the native executable using the Docker.
mvn package -Dnative -Dquarkus.native.container-build

# Run the native executable.
version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
./target/quarkus-azure-integration-test-services-together-${version}-runner
```

## Testing the sample

Open a new terminal and run the following commands to test the sample:

```
# Upload a blob with "Hello Quarkus Azure Storage Blob!" to Azure Blob Storage.
curl http://localhost:8080/quarkus-services-azure-storage-blob/testcontainer/testblob -X POST -d 'Hello Quarkus Azure Storage Blob!' -H "Content-Type: text/plain"

# Download the blob from Azure Blob Storage. You should see "Hello Quarkus Azure Storage Blob!" in the response.
curl http://localhost:8080/quarkus-services-azure-storage-blob/testcontainer/testblob -X GET

# Delete the blob from Azure Blob Storage.
curl http://localhost:8080/quarkus-services-azure-storage-blob/testcontainer/testblob -X DELETE

# Download the blob from Azure Blob Storage again. You should see "404 Not Found" in the response.
curl http://localhost:8080/quarkus-services-azure-storage-blob/testcontainer/testblob -X GET -I

# Upload a blob with "Hello Quarkus Azure Storage Blob Async!" to Azure Blob Storage using the async API.
curl http://localhost:8080/quarkus-services-azure-storage-blob-async/testcontainer-async/testblob-async -X POST -d 'Hello Quarkus Azure Storage Blob Async!' -H "Content-Type: text/plain"

# Download the blob from Azure Blob Storage using the async API. You should see "Hello Quarkus Azure Storage Blob Async!" in the response.
curl http://localhost:8080/quarkus-services-azure-storage-blob-async/testcontainer-async/testblob-async -X GET

# Delete the blob from Azure Blob Storage using the async API.
curl http://localhost:8080/quarkus-services-azure-storage-blob-async/testcontainer-async/testblob-async -X DELETE

# Download the blob from Azure Blob Storage again using the async API. You should see "404 Not Found" in the response.
curl http://localhost:8080/quarkus-services-azure-storage-blob-async/testcontainer-async/testblob-async -X HEAD -I

# Get the secret "secret1" from Azure Key Vault using the secret client. You should see "mysecret" in the response.
curl http://localhost:8080/quarkus-services-azure-key-vault/getSecretBySecretClient -X GET

# Get the secret "secret1" from Azure Key Vault using the configuration property. You should see "mysecret" in the response.
curl http://localhost:8080/quarkus-services-azure-key-vault/getSecretByConfigProperty -X GET
```

Press `Ctrl + C` to stop the sample once you complete the try and test.

## Run tests

Besides running the sample and testing it manually, you can also run the tests to verify the sample.

> **NOTE:** Make sure you executed all previous steps before running the tests.

Run the following command to run the tests:

```
# Run the integration tests in native mode
mvn test-compile failsafe:integration-test failsafe:verify -Dnative -Dazure.test=true

# Run the unit tests and integration tests in JVM mode
mvn verify -Dazure.test=true
```

## Cleaning up Azure resources

Run the following command to clean up the Azure resources if you created before:

```
az group delete \
    --name ${RESOURCE_GROUP_NAME} \
    --yes --no-wait
```
