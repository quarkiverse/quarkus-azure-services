# Azure Key Vault sample

This is a sample application using the Quarkus Key Vault extension to create secret with `SecretClient` and `SecretAsyncClient` from Azure Key Vault.

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

You need to create an Azure Key Vault before running the sample application.

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

Run the following commands to create an Azure Key Vault, set permission and export its endpoint as an environment
variable. You can also find the same AZ CLI commands to create Azure Key Vault services in `.github/build-with-maven-native.sh`.

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

## ## Testing the sample

Open a new terminal and run the following commands to test the sample:

```
#Use SecretClient to create a secret and get the value:
curl http://localhost:8080/keyvault/sync

#Use SecretAsyncClient to create a secret and get the value:
curl http://localhost:8080/keyvault/async
```

Now you will 

Press `Ctrl + C` to stop the sample once you complete the try and test.

## Cleaning up Azure resources

Run the following command to clean up the Azure resources if you created before:

```
az group delete \
    --name ${RESOURCE_GROUP_NAME} \
    --yes --no-wait
```