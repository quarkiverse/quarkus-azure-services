# Azure Service Bus sample

This is a sample about using the Quarkus extension to send/receive messages with Azure Service Bus.

## Prerequisites

To successfully run this sample, you need:

* JDK 17+ installed with JAVA_HOME configured appropriately
* Apache Maven 3.8.6+
* Azure CLI and Azure subscription
* Docker

You also need to clone the repository and switch to the directory of the sample.

```
git clone https://github.com/quarkiverse/quarkus-azure-services.git
cd quarkus-azure-services/integration-tests/azure-servicebus
```

### Use development iteration version

By default, the sample is kept in sync with the development iteration version, which is `999-SNAPSHOT`. To install the development iteration version, build it locally.

```
mvn clean install -DskipTests --file ../../pom.xml
```

### Use release version

If you want to use the release version, you need to update the version of dependencies in the `pom.xml` file.

First, you need to find out the latest release version of the Quarkus Azure services extensions from [releases](https://github.com/quarkiverse/quarkus-azure-services/releases), for example, `1.1.6`.

Then, update the version of dependencies in the `pom.xml` file, for example:

```xml
<parent>
    <groupId>io.quarkiverse.azureservices</groupId>
    <artifactId>quarkus-azure-services-parent</artifactId>
    <version>1.1.6</version>
    <relativePath></relativePath>
</parent>
```

## Preparing the Azure services

You need to create an Azure Service Bus before running the sample application.

### Logging into Azure

Log into Azure and create a resource group for hosting the Azure Service Bus.

```
az login

RESOURCE_GROUP_NAME=<resource-group-name>
az group create \
    --name ${RESOURCE_GROUP_NAME} \
    --location westus
```

### Creating Azure Service Bus

Run the following commands to create an Azure Service Bus namespace and a queue.

```
SERVICEBUS_NAMESPACE=<unique-servicebus-namespace>
az servicebus namespace create \
    --name ${SERVICEBUS_NAMESPACE} \
    --resource-group ${RESOURCE_GROUP_NAME}

az servicebus queue create \
    --name test-queue \
    --namespace-name ${SERVICEBUS_NAMESPACE} \
    --resource-group ${RESOURCE_GROUP_NAME}
```

You have two options to authenticate to Azure Service Bus, either with Microsoft Entra ID or connection string. The following sections describe how to authenticate with both options. For optimal security, it is recommended to use Microsoft Entra ID for authentication.

#### Authenticating to Azure Service Bus with Microsoft Entra ID

You can authenticate to Azure Service Bus with Microsoft Entra ID. Run the following commands to assign the `Azure Service Bus Data Owner` role to the signed-in user as a Microsoft Entra identity.

```
SERVICEBUS_RESOURCE_ID=$(az servicebus namespace show \
    --resource-group $RESOURCE_GROUP_NAME \
    --name $SERVICEBUS_NAMESPACE \
    --query 'id' \
    --output tsv)
OBJECT_ID=$(az ad signed-in-user show --query id -o tsv)

az role assignment create \
    --role "Azure Service Bus Data Owner" \
    --assignee ${OBJECT_ID} \
    --scope $SERVICEBUS_RESOURCE_ID
```

Run the following commands to export the namespace of the Azure Service Bus as an environment variable.

```
export QUARKUS_AZURE_SERVICEBUS_NAMESPACE=${SERVICEBUS_NAMESPACE}
```

The value of environment variable `QUARKUS_AZURE_SERVICEBUS_NAMESPACE` will be read by Quarkus as the value of config property `quarkus.azure.servicebus.namespace` of `azure-servicebus` extension in order to set up the connection to the Azure Service Bus.

#### Authenticating to Azure Service Bus with connection string

You can also authenticate to Azure Service Bus with connection string. Run the following commands to export the connection string of the Azure Service Bus namespace as an environment variable.

```
export QUARKUS_AZURE_SERVICEBUS_CONNECTION_STRING=$(az servicebus namespace authorization-rule keys list \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    --namespace-name "${SERVICEBUS_NAMESPACE}" \
    --name RootManageSharedAccessKey \
    --query primaryConnectionString -o tsv)
```

The value of environment variable `QUARKUS_AZURE_SERVICEBUS_CONNECTION_STRING` will be read by Quarkus as the value of config property `quarkus.azure.servicebus.connection-string` of `azure-servicebus` extension in order to set up the connection to the Azure Service Bus.

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
./target/quarkus-azure-integration-test-servicebus-${version}-runner
```

## Testing the sample

Open a new terminal and run the following commands to test the sample:

```
# Send/process message "Hello Azure Service Bus!" to/from Azure Service Bus.
curl http://localhost:8080/quarkus-azure-servicebus/messages -X POST -d '{"message": "Hello Azure Service Bus!"}' -H "Content-Type: application/json"

# Retrieve the cached message that is received from Azure Service Bus. You should see {"messages":["Hello Azure Service Bus!"],"count":1,"status":"success"} in the response.
curl http://localhost:8080/quarkus-azure-servicebus/messages
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

Run the following command to clean up the Azure resources you created before:

```
az group delete \
    --name ${RESOURCE_GROUP_NAME} \
    --yes --no-wait
```
