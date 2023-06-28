# Azure App Configuration sample

This is a sample about implementing REST endpoints using the Quarkus extension to get the configuration stored in Azure
App Configuration.

## Prerequisites

To successfully run this sample, you need:

* JDK 11+ installed with JAVA_HOME configured appropriately
* Apache Maven 3.8.6+
* Azure CLI and Azure subscription if the specific Azure services are required
* Docker if you want to build the app as a native executable

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
    --location eastus
```

### Creating Azure App Configuration store

Run the following commands to create an Azure App Configuration store, add a few key-value pairs, and export its
connection info as environment variables.

```
export APP_CONFIG_NAME=<unique-app-config-name>
az appconfig create \
    --name "${APP_CONFIG_NAME}" \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    --location eastus

az appconfig kv set \
    --name "${APP_CONFIG_NAME}" \
    --key my.prop \
    --value 1234 \
    --yes
az appconfig kv set \
    --name "${APP_CONFIG_NAME}" \
    --key another.prop \
    --value 5678 \
    --yes
    
export QUARKUS_AZURE_APP_CONFIGURATION_ENDPOINT=$(az appconfig show \
  --resource-group "${RESOURCE_GROUP_NAME}" \
  --name "${APP_CONFIG_NAME}" \
  --query endpoint -o tsv)
credential=$(az appconfig credential list \
    --name "${APP_CONFIG_NAME}" \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    | jq 'map(select(.readOnly == true)) | .[0]')
export QUARKUS_AZURE_APP_CONFIGURATION_ID=$(echo "${credential}" | jq -r '.id')
export QUARKUS_AZURE_APP_CONFIGURATION_SECRET=$(echo "${credential}" | jq -r '.value')
```

The values of environment
variable `QUARKUS_AZURE_APP_CONFIGURATION_ENDPOINT` / `QUARKUS_AZURE_APP_CONFIGURATION_ID` / `QUARKUS_AZURE_APP_CONFIGURATION_SECRET`
will be fed into config
properties `quarkus.azure.app.configuration.endpoint` / `quarkus.azure.app.configuration.id` / `quarkus.azure.app.configuration.secret`
of `azure-app-configuration` extension in order to set up the connection to the Azure App Configuration store.

## Running the sample

You have different choices to run the sample.

### Running the sample in development mode

First, you can launch the sample in `dev` mode.

```
mvn -Djvm.args="-Dvertx.disableDnsResolver=true" quarkus:dev
```

### Running the sample in JVM mode

You can also run the sample in JVM mode.

```
# Build the package.
mvn package

# Run the generated jar file.
java -Dvertx.disableDnsResolver=true -jar ./target/quarkus-app/quarkus-run.jar
```

### Running the sample as a native executable

You can even run the sample as a native executable. Make sure you have installed Docker.

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

## Cleaning up Azure resources

Run the following command to clean up the Azure resources if you created before:

```
az group delete \
    --name ${RESOURCE_GROUP_NAME} \
    --yes --no-wait
```
