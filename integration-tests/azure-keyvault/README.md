# Azure Key Vault sample

This is a sample application using the Quarkus Key Vault extension to create secret with `SecretClient` and `SecretAsyncClient` from Azure Key Vault.

## Prerequisites

To successfully run this sample, you need:

* JDK 17+ installed with JAVA_HOME configured appropriately
* Apache Maven 3.8.6+
* Azure CLI and Azure subscription
* Docker

You also need to clone the repository and switch to the directory of the sample.

```
git clone https://github.com/quarkiverse/quarkus-azure-services.git
cd quarkus-azure-services/integration-tests/azure-keyvault
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
from [releases](https://github.com/quarkiverse/quarkus-azure-services/releases), for example, `1.1.1`.

Then, update the version of dependencies in the `pom.xml` file, for example:

```xml
<parent>
    <groupId>io.quarkiverse.azureservices</groupId>
    <artifactId>quarkus-azure-services-parent</artifactId>
    <version>1.1.1</version>
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

Run the following commands to create an Azure Key Vault and export its endpoint as an environment variable.

```
KEY_VAULT_NAME=<unique-key-vault-name>
az keyvault create --name ${KEY_VAULT_NAME} \
    --resource-group ${RESOURCE_GROUP_NAME} \
    --location eastus \
    --enable-rbac-authorization false

export QUARKUS_AZURE_KEYVAULT_SECRET_ENDPOINT=$(az keyvault show --name ${KEY_VAULT_NAME}\
    --resource-group ${RESOURCE_GROUP_NAME}\
    --query properties.vaultUri -otsv)
echo "The value of 'quarkus.azure.keyvault.secret.endpoint' is: ${QUARKUS_AZURE_KEYVAULT_SECRET_ENDPOINT}"
```

Add secret `secret1` with value `mysecret`.

```
az keyvault secret set \
    --vault-name ${KEY_VAULT_NAME} \
    --name secret1 \
    --value mysecret
```

The value of environment variable `QUARKUS_AZURE_KEYVAULT_SECRET_ENDPOINT` will be fed into config
property `quarkus.azure.keyvault.secret.endpoint` of `azure-keyvault` extension in order to set up the
connection to the Azure Key Vault.

You can also manually copy the output of the variable `quarkus.azure.keyvault.secret.endpoint` and then
update [application.properties](src/main/resources/application.properties) by uncommenting the
same property and setting copied value.

To access the secret, you can use the SecretClient or abtain it as a property.

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
./target/quarkus-azure-integration-test-keyvault-secret-${version}-runner
```

## Testing the sample

Open a new terminal and run the following commands to test the sample:

```
#Use SecretClient to create a secret and get the value:
curl http://localhost:8080/keyvault/sync

#Use SecretAsyncClient to create a secret and get the value:
curl http://localhost:8080/keyvault/async

#Use config property to get the value of secret1:
curl http://localhost:8080/keyvaultConfig/getSecret
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