# Azure App Configuration sample

This is a sample about implementing REST endpoints using the Quarkus extension to get the configuration stored in Azure
App Configuration.

## Prerequisites

To successfully run this sample, you need:

* JDK 17+ installed with JAVA_HOME configured appropriately
* Apache Maven 3.8.6+
* Azure CLI and Azure subscription
* Docker

You also need to clone the repository and switch to the directory of the sample.

```
git clone https://github.com/quarkiverse/quarkus-azure-services.git
cd quarkus-azure-services/integration-tests/azure-app-configuration
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
from [releases](https://github.com/quarkiverse/quarkus-azure-services/releases), for example, `1.1.5`.

Then, update the version of dependencies in the `pom.xml` file, for example:

```xml
<parent>
    <groupId>io.quarkiverse.azureservices</groupId>
    <artifactId>quarkus-azure-services-parent</artifactId>
    <version>1.1.5</version>
    <relativePath></relativePath>
</parent>
```

## Preparing the Azure services

The Quarkus Azure app configuration extension needs to connect to a real Azure app configuration store, follow steps
below to create one.

### Logging into Azure

Log into Azure and create a resource group for hosting the Azure App Configuration store to be created.

```
az login

RESOURCE_GROUP_NAME=<resource-group-name>
az group create \
    --name ${RESOURCE_GROUP_NAME} \
    --location centralus
```

### Creating Azure App Configuration store

Run the following commands to create an Azure App Configuration store, add a few key-value pairs, and export its endpoint as environment variable.

```
APP_CONFIG_NAME=<unique-app-config-name>
az appconfig create \
    --name "${APP_CONFIG_NAME}" \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    --location centralus

az appconfig kv set \
    --name "${APP_CONFIG_NAME}" \
    --key my.prop \
    --value 1234 \
    --yes
az appconfig kv set \
    --name "${APP_CONFIG_NAME}" \
    --key another.prop \
    --value 5678 \
    --label prod \
    --yes
    
export QUARKUS_AZURE_APP_CONFIGURATION_ENDPOINT=$(az appconfig show \
  --resource-group "${RESOURCE_GROUP_NAME}" \
  --name "${APP_CONFIG_NAME}" \
  --query endpoint -o tsv)
```

The values of environment variable `QUARKUS_AZURE_APP_CONFIGURATION_ENDPOINT` will be fed into config property `quarkus.azure.app.configuration.endpoint` of `azure-app-configuration` extension in order to set up the connection to the Azure App Configuration store.

You have two options to authenticate to Azure App Configuration,, either with Microsoft Entra ID or with access keys. The following sections describe how to authenticate with both options. For optimal security, it is recommended to use Microsoft Entra ID for authentication.

#### Authenticating to Azure App Configuration with Microsoft Entra ID

You can authenticate to Azure App Configuration with Microsoft Entra ID. Run the following commands to assign the `App Configuration Data Reader` role to the signed-in user as a Microsoft Entra identity.

```
# Retrieve the app configuration resource ID
APP_CONFIGURATION_RESOURCE_ID=$(az appconfig show \
    --resource-group $RESOURCE_GROUP_NAME \
    --name "${APP_CONFIG_NAME}" \
    --query 'id' \
    --output tsv)
# Assign the "App Configuration Data Reader" role to the current signed-in identity
az role assignment create \
    --assignee $(az ad signed-in-user show --query 'id' --output tsv) \
    --role "App Configuration Data Reader" \
    --scope $APP_CONFIGURATION_RESOURCE_ID
```

#### Authenticating to Azure App Configuration with access keys

You can also authenticate to Azure App Configuration with access keys. Run the following commands to export the Azure App Configuration access keys as environment variables.

```
credential=$(az appconfig credential list \
    --name "${APP_CONFIG_NAME}" \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    | jq 'map(select(.readOnly == true)) | .[0]')
export QUARKUS_AZURE_APP_CONFIGURATION_ID=$(echo "${credential}" | jq -r '.id')
export QUARKUS_AZURE_APP_CONFIGURATION_SECRET=$(echo "${credential}" | jq -r '.value')
```

The values of environment variable `QUARKUS_AZURE_APP_CONFIGURATION_ID` and `QUARKUS_AZURE_APP_CONFIGURATION_SECRET` will be fed into config properties `quarkus.azure.app.configuration.id` and `quarkus.azure.app.configuration.secret` of `azure-app-configuration` extension in order to set up the connection to the Azure App Configuration store.

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
./target/quarkus-azure-integration-test-app-configuration-${version}-runner
```

## Testing the sample

Open a new terminal and run the following commands to test the sample:

```
# Get the value of property "my.prop" stored in the Azure app configuration. You should see "1234" in the response.
curl http://localhost:8080/config/my.prop -X GET

# Get the value of property "another.prop" stored in the Azure app configuration. You should see "5678" in the response.
curl http://localhost:8080/config/another.prop -X GET
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
