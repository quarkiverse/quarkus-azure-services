# Azure Blob Storage sample

This is a sample about implementing REST endpoints using the Quarkus Azure storage blob extension to upload and download
files to/from Azure Blob Storage.

## Prerequisites

To successfully run this sample, you need:

* JDK 17+ installed with JAVA_HOME configured appropriately
* Apache Maven 3.8.6+
* Azure CLI and Azure subscription
* Docker

You also need to clone the repository and switch to the directory of the sample.

```
git clone https://github.com/quarkiverse/quarkus-azure-services.git
cd quarkus-azure-services/integration-tests/azure-storage-blob
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
from [releases](https://github.com/quarkiverse/quarkus-azure-services/releases), for example, `1.0.7`.

Then, update the version of dependencies in the `pom.xml` file, for example:

```xml
<parent>
    <groupId>io.quarkiverse.azureservices</groupId>
    <artifactId>quarkus-azure-services-parent</artifactId>
    <version>1.0.7</version>
    <relativePath></relativePath>
</parent>
```

## Preparing the Azure services

The Quarkus Azure storage blob extension supports **Dev Services**, it allows to run the sample without the real Azure
storage blob created and configured in the `dev` or `test` modes, you can go
to [Running the sample in development mode](#running-the-sample-in-development-mode) and have a try. However, if you
want to run the sample in JVM mode or as a native executable, you need to prepare the Azure storage blob first.

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

Run the following commands to create an Azure Storage Account and export its connection string as an environment
variable. You can also find the same AZ CLI commands to create Azure Storage Account service in `.github/build-with-maven-native.sh`.

```
STORAGE_ACCOUNT_NAME=<unique-storage-account-name>
az storage account create \
    --name ${STORAGE_ACCOUNT_NAME} \
    --resource-group ${RESOURCE_GROUP_NAME} \
    --location eastus \
    --sku Standard_LRS \
    --kind StorageV2
```

You have two options to authenticate to Azure Storage Blob, either with Microsoft Entra ID or with connection string. The following sections describe how to authenticate with both options. For optimal security, it is recommended to use Microsoft Entra ID for authentication.

#### Authenticating to Azure Storage Blob with Microsoft Entra ID

You can authenticate to Azure Storage Blob with Microsoft Entra ID. Run the following commands to assign the `Storage Blob Data Contributor` role to the signed-in user as a Microsoft Entra identity.

```
# Retrieve the storage account resource ID
STORAGE_ACCOUNT_RESOURCE_ID=$(az storage account show \
    --resource-group $RESOURCE_GROUP_NAME \
    --name $STORAGE_ACCOUNT_NAME \
    --query 'id' \
    --output tsv)
# Assign the "Storage Blob Data Contributor" role to the current signed-in identity
az role assignment create \
    --assignee $(az ad signed-in-user show --query 'id' --output tsv) \
    --role "Storage Blob Data Contributor" \
    --scope $STORAGE_ACCOUNT_RESOURCE_ID
```

Then, export Azure Storage Blob endpoint as an environment variable.

```
export QUARKUS_AZURE_STORAGE_BLOB_ENDPOINT=$(az storage account show \
    --resource-group $RESOURCE_GROUP_NAME \
    --name $STORAGE_ACCOUNT_NAME \
    --query 'primaryEndpoints.blob' \
    --output tsv)
echo "The value of 'quarkus.azure.storage.blob.endpoint' is: ${QUARKUS_AZURE_STORAGE_BLOB_ENDPOINT}"
```

The value of environment variable `QUARKUS_AZURE_STORAGE_BLOB_ENDPOINT` will be read by Quarkus as the value of config
property `quarkus.azure.storage.blob.endpoint` of `azure-storage-blob` extension in order to set up the
connection to the Azure Storage Blob.

#### Authenticating to Azure Storage Blob with connection string

You can also authenticate to Azure Storage Blob with connection string. Run the following commands to export the Azure Storage Blob connection string as an environment variable.

```
export QUARKUS_AZURE_STORAGE_BLOB_CONNECTION_STRING=$(az storage account show-connection-string \
    --resource-group ${RESOURCE_GROUP_NAME} \
    --name ${STORAGE_ACCOUNT_NAME} \
    --query connectionString -o tsv)
echo "The value of 'quarkus.azure.storage.blob.connection-string' is: ${QUARKUS_AZURE_STORAGE_BLOB_CONNECTION_STRING}"
```

The value of environment variable `QUARKUS_AZURE_STORAGE_BLOB_CONNECTION_STRING` will be fed into config
property `quarkus.azure.storage.blob.connection-string` of `azure-storage-blob` extension in order to set up the connection to the Azure Storage Blob.

## Running the sample

You have different choices to run the sample.

### Running the sample in development mode

First, you can launch the sample in `dev` mode.

```
mvn quarkus:dev
```

### Running the sample in JVM mode

You can also run the sample in JVM mode. Make sure you have
followed [Preparing the Azure services](#preparing-the-azure-services) to create the required Azure services.

```
# Build the package.
mvn package

# Run the generated jar file.
java -jar ./target/quarkus-app/quarkus-run.jar
```

### Running the sample as a native executable

You can even run the sample as a native executable. Make sure you have installed Docker and
followed [Preparing the Azure services](#preparing-the-azure-services) to create the required Azure services.

```
# Build the native executable using the Docker.
mvn package -Dnative -Dquarkus.native.container-build

# Run the native executable.
version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
./target/quarkus-azure-integration-test-storage-blob-${version}-runner
```

## Testing the sample

Open a new terminal and run the following commands to test the sample:

```
# Upload a blob with "Hello Quarkus Azure Storage Blob!" to Azure Blob Storage.
curl http://localhost:8080/quarkus-azure-storage-blob/testcontainer/testblob -X POST -d 'Hello Quarkus Azure Storage Blob!' -H "Content-Type: text/plain"

# Download the blob from Azure Blob Storage. You should see "Hello Quarkus Azure Storage Blob!" in the response.
curl http://localhost:8080/quarkus-azure-storage-blob/testcontainer/testblob -X GET

# Delete the blob from Azure Blob Storage.
curl http://localhost:8080/quarkus-azure-storage-blob/testcontainer/testblob -X DELETE

# Download the blob from Azure Blob Storage again. You should see "404 Not Found" in the response.
curl http://localhost:8080/quarkus-azure-storage-blob/testcontainer/testblob -X GET -I

# Upload a blob with "Hello Quarkus Azure Storage Blob Async!" to Azure Blob Storage using the async API.
curl http://localhost:8080/quarkus-azure-storage-blob-async/testcontainer-async/testblob-async -X POST -d 'Hello Quarkus Azure Storage Blob Async!' -H "Content-Type: text/plain"

# Download the blob from Azure Blob Storage using the async API. You should see "Hello Quarkus Azure Storage Blob Async!" in the response.
curl http://localhost:8080/quarkus-azure-storage-blob-async/testcontainer-async/testblob-async -X GET

# Delete the blob from Azure Blob Storage using the async API.
curl http://localhost:8080/quarkus-azure-storage-blob-async/testcontainer-async/testblob-async -X DELETE

# Download the blob from Azure Blob Storage again using the async API. You should see "404 Not Found" in the response.
curl http://localhost:8080/quarkus-azure-storage-blob-async/testcontainer-async/testblob-async -X HEAD -I
```

Press `Ctrl + C` to stop the sample once you complete the try and test.

## Cleaning up Azure resources

Run the following command to clean up the Azure resources if you created before:

```
az group delete \
    --name ${RESOURCE_GROUP_NAME} \
    --yes --no-wait
```
