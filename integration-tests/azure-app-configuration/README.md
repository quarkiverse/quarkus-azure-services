# Azure App Configuration sample

This is a sample about implementing REST endpoints using the Quarkus extension to get the configuration stored in Azure
App Configuration.

## Prerequisites

To successfully run this sample, you need:

* JDK 17+ installed with JAVA_HOME configured appropriately
* Apache Maven 3.8.6+
* Azure CLI and Azure subscription
* Docker if you want to build the app as a native executable

You also need to make sure the right version of dependencies are installed.

### Use development iteration version

By default, the sample depends on the development iteration version, which is `999-SNAPSHOT`. To install the development
iteration version, you need to build it locally.

```
# Switch to the root directory of Quarkus Azure services extensions.
# For example, if you are in the directory of quarkus-azure-services/integration-tests/azure-app-configuration
cd ../..

# Install all Quarkus Azure services extensions locally.
mvn clean install -DskipTests

# Switch back to the directory of integration-tests/azure-app-configuration
cd integration-tests/azure-app-configuration
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

Run the following commands to create an Azure App Configuration store, add a few key-value pairs, and export its
connection info as environment variables. You can also find the same AZ CLI commands to create Azure app configuration service in `.github/build-with-maven-native.sh`.

```
export APP_CONFIG_NAME=<unique-app-config-name>
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
