# Azure Cosmos DB sample

This is a sample about implementing REST endpoints using the Quarkus extension to read/write data stored in Azure Cosmos DB. Though the sample uses a relational style usage pattern, the full functionality of Cosmos DB is enabled by the Quarkus extension.

## Prerequisites

To successfully run this sample, you need:

* JDK 17+ installed with JAVA_HOME configured appropriately
* Apache Maven 3.8.6+
* Azure CLI and Azure subscription
* Docker

You also need to clone the repository and switch to the directory of the sample.

```
git clone https://github.com/quarkiverse/quarkus-azure-services.git
cd quarkus-azure-services/integration-tests/azure-cosmos
```

### Use development iteration version

By default, the sample is kept in sync with the development iteration
version, which is `999-SNAPSHOT`. To install the development iteration
version, build it locally.

```
mvn clean install -DskipTests --file ../../pom.xml
```

### Use release version

If you want to use the release version, you need to update the version of dependencies in the `pom.xml` file.

First, you need to find out the latest release version of the Quarkus Azure services extensions
from [releases](https://github.com/quarkiverse/quarkus-azure-services/releases), for example, `1.1.4`.

Then, update the version of dependencies in the `pom.xml` file, for example:

```xml
<parent>
    <groupId>io.quarkiverse.azureservices</groupId>
    <artifactId>quarkus-azure-services-parent</artifactId>
    <version>1.1.4</version>
    <relativePath></relativePath>
</parent>
```

## Preparing the Azure services

You need to create an Azure Cosmos DB account before running the sample application.

### Logging into Azure

Log into Azure and create a resource group for hosting the Azure Cosmos DB account to be created.

```
az login

RESOURCE_GROUP_NAME=<resource-group-name>
az group create \
    --name ${RESOURCE_GROUP_NAME} \
    --location westus
```

### Creating Azure Azure Cosmos DB account

Run the following commands to create an Azure Cosmos DB account, and export its endpoint as an environment variable.

```
COSMOSDB_ACCOUNT_NAME=<unique-cosmosdb-account-name>
az cosmosdb create \
    -n ${COSMOSDB_ACCOUNT_NAME} \
    -g ${RESOURCE_GROUP_NAME} \
    --default-consistency-level Session \
    --locations regionName='West US' failoverPriority=0 isZoneRedundant=False

export QUARKUS_AZURE_COSMOS_ENDPOINT=$(az cosmosdb show \
    -n ${COSMOSDB_ACCOUNT_NAME} \
    -g ${RESOURCE_GROUP_NAME} \
    --query documentEndpoint -o tsv)
echo "The value of 'quarkus.azure.cosmos.endpoint' is: ${QUARKUS_AZURE_COSMOS_ENDPOINT}"
```

The value of environment variable `QUARKUS_AZURE_COSMOS_ENDPOINT` will be read by Quarkus as the value of config
property `quarkus.azure.cosmos.endpoint` of `azure-cosmos` extension in order to set up the
connection to the Azure Cosmos DB.

You have two options to authenticate to Azure Cosmos DB, either with Microsoft Entra ID or key-based authentication. The following sections describe how to authenticate with both options. For optimal security, it is recommended to use Microsoft Entra ID for authentication.

#### Authenticating to Azure Cosmos DB with Microsoft Entra ID

You can authenticate to Azure Cosmos DB with Microsoft Entra ID. Run the following commands to assign the `Cosmos DB Built-in Data Contributor` role to the signed-in user as a Microsoft Entra identity.

```
az ad signed-in-user show --query id -o tsv \
    | az cosmosdb sql role assignment create \
    --account-name ${COSMOSDB_ACCOUNT_NAME} \
    --resource-group ${RESOURCE_GROUP_NAME} \
    --scope "/" \
    --principal-id @- \
    --role-definition-id 00000000-0000-0000-0000-000000000002
```

You cannot use any Azure Cosmos DB data plane SDK to authenticate management operations with a Microsoft Entra identity, so you need to create database and container manually.
The following commands create a database `demodb` and a container `democontainer` using Azure CLI.

```
az cosmosdb sql database create \
    -a ${COSMOSDB_ACCOUNT_NAME} \
    -g ${RESOURCE_GROUP_NAME} \
    -n demodb
az cosmosdb sql container create \
    -a ${COSMOSDB_ACCOUNT_NAME} \
    -g ${RESOURCE_GROUP_NAME} \
    -d demodb \
    -n democontainer \
    -p "/id"
```

#### Authenticating to Azure Cosmos DB with key-based authentication

You can also authenticate to Azure Cosmos DB with key-based authentication. Run the following commands to export the key of the Azure Cosmos DB account as an environment variable.

```
export QUARKUS_AZURE_COSMOS_KEY=$(az cosmosdb keys list \
    -n ${COSMOSDB_ACCOUNT_NAME} \
    -g ${RESOURCE_GROUP_NAME} \
    --query primaryMasterKey -o tsv)
```

The value of environment variable `QUARKUS_AZURE_COSMOS_KEY` will be read by Quarkus as the value of config property `quarkus.azure.cosmos.key` of `azure-cosmos` extension in order to set up the connection to the Azure Cosmos DB.

You do not need to create a database and container manually if you use key-based authentication, because it has full access to the Azure Cosmos DB account. The sample application will create the database and container automatically.

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
./target/quarkus-azure-integration-test-cosmos-${version}-runner
```

## Testing the sample

Open a new terminal and run the following commands to test the sample:

```
# Create an item {"id": "1", "name": "dog"} in Azure Cosmos DB database demodb and container democontainer.
curl http://localhost:8080/quarkus-azure-cosmos/demodb/democontainer -X POST -d '{"id": "1", "name": "dog"}' -H "Content-Type: application/json"

# Read the item from Azure Cosmos DB database demodb and container democontainer. You should see {"id":"1","name":"dog"} in the response.
curl http://localhost:8080/quarkus-azure-cosmos/demodb/democontainer/1 -X GET

# List items from Azure Cosmos DB database demodb and container democontainer. You should see [{"id":"1","name":"dog"}] in the response.
curl http://localhost:8080/quarkus-azure-cosmos/demodb/democontainer -X GET

# Update the item {"id": "1", "name": "dog"} to {"id": "1", "name": "cat"} in Azure Cosmos DB database demodb and container democontainer.
curl http://localhost:8080/quarkus-azure-cosmos/demodb/democontainer -X POST -d '{"id": "1", "name": "cat"}' -H "Content-Type: application/json"

# Read the updated item from Azure Cosmos DB database demodb and container democontainer. You should see {"id":"1","name":"cat"} in the response.
curl http://localhost:8080/quarkus-azure-cosmos/demodb/democontainer/1 -X GET

# List items again from Azure Cosmos DB database demodb and container democontainer. You should see [{"id":"1","name":"cat"}] in the response.
curl http://localhost:8080/quarkus-azure-cosmos/demodb/democontainer -X GET

# Delete the item from Azure Cosmos DB database demodb and container democontainer.
curl http://localhost:8080/quarkus-azure-cosmos/demodb/democontainer/1 -X DELETE

# List items again from Azure Cosmos DB database demodb and container democontainer. You should see [] in the response.
curl http://localhost:8080/quarkus-azure-cosmos/demodb/democontainer -X GET
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
