# Azure Event Hubs sample

This is a sample about implementing REST endpoints using the Quarkus extension to send/receive data to/from Azure Event Hubs.

## Prerequisites

To successfully run this sample, you need:

* JDK 17+ installed with JAVA_HOME configured appropriately
* Apache Maven 3.8.6+
* Azure CLI and Azure subscription
* Docker

You also need to clone the repository and switch to the directory of the sample.

```
git clone https://github.com/quarkiverse/quarkus-azure-services.git
cd quarkus-azure-services/integration-tests/azure-eventhubs
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
from [releases](https://github.com/quarkiverse/quarkus-azure-services/releases), for example, `1.1.0`.

Then, update the version of dependencies in the `pom.xml` file, for example:

```xml
<parent>
    <groupId>io.quarkiverse.azureservices</groupId>
    <artifactId>quarkus-azure-services-parent</artifactId>
    <version>1.1.0</version>
    <relativePath></relativePath>
</parent>
```

## Preparing the Azure services

You need to create an Azure Eventhub namespace before running the sample application.

### Logging into Azure

Log into Azure and create a resource group for hosting the Azure Eventhub namespace to be created.

```
az login

RESOURCE_GROUP_NAME=<resource-group-name>
az group create \
    --name ${RESOURCE_GROUP_NAME} \
    --location westus
```

### Creating Azure Event Hubs resources

Run the following commands to create an Azure eventhub namespace and an eventhub within the namespace, and export the environment variables to be used in the sample application.

```
EVENTHUBS_NAMESPACE=<unique-eventhub-namespace-name>
EVENTHUBS_EVENTHUB_NAME=<unique-eventhub-name>
# Azure Event Hubs Extension
az eventhubs namespace create \
    --name ${EVENTHUBS_NAMESPACE} \
    --resource-group ${RESOURCE_GROUP_NAME}
az eventhubs eventhub create \
    --name ${EVENTHUBS_EVENTHUB_NAME} \
    --namespace-name ${EVENTHUBS_NAMESPACE} \
    --resource-group ${RESOURCE_GROUP_NAME} \
    --partition-count 2

export QUARKUS_AZURE_EVENTHUBS_NAMESPACE=${EVENTHUBS_NAMESPACE}
export QUARKUS_AZURE_EVENTHUBS_EVENTHUB_NAME=${EVENTHUBS_EVENTHUB_NAME}
```

Assign the `Azure Event Hubs Data Owner` role to the signed-in user as a Microsoft Entra identity, so that the sample application can do data plane operations.

```
EVENTHUBS_EVENTHUB_RESOURCE_ID=$(az eventhubs eventhub show \
    --resource-group $RESOURCE_GROUP_NAME \
    --namespace-name $EVENTHUBS_NAMESPACE \
    --name $EVENTHUBS_EVENTHUB_NAME \
    --query 'id' \
    --output tsv)
az role assignment create \
    --role "Azure Event Hubs Data Owner" \
    --assignee $(az ad signed-in-user show --query 'id' --output tsv) \
    --scope $EVENTHUBS_EVENTHUB_RESOURCE_ID
```

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
./target/quarkus-azure-integration-test-eventhubs-${version}-runner
```

## Testing the sample

Open a new terminal and run the following commands to test the sample:

```
# Send a message to the Azure Event Hubs with Sync API.
curl http://localhost:8080/quarkus-azure-eventhubs/sendEvents -X GET

# Receive messages from the Azure Event Hubs with Sync API.
curl http://localhost:8080/quarkus-azure-eventhubs/receiveEvents -X GET

# Send a message to the Azure Event Hubs with Async API.
curl http://localhost:8080/quarkus-azure-eventhubs-async/sendEvents -X GET

# Receive messages from the Azure Event Hubs with Async API.
curl http://localhost:8080/quarkus-azure-eventhubs-async/receiveEvents -X GET
```

Press `Ctrl + C` to stop the sample once you complete the try and test.

## Run tests

Besides running the sample and testing it manually, you can also run the tests to verify the sample.

> **NOTE:** Make sure you executed all previous steps before running the tests.

Run the following command to run the tests:

```
# Run the integration tests in native mode
mvn test-compile failsafe:integration-test -Dnative -Dazure.test=true

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
