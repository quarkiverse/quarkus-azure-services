# Quarkus Azure Services - Integration Tests

This is the integration test for testing all Quarkus Azure services extensions from REST endpoints.

## Running the test with Dev services

Some Quarkus Azure services extensions support Dev Services, it allows to test easily.
Launch these tests with:

```
mvn test
```

## Running the test with Azure services

If you want to run all the test with real Azure services in the native mode, you need to create the dependent services
on Azure after logging into Azure.

### Logging into Azure

Log into Azure and create a resource group for hosting different Azure services to be created.

```
az login

RESOURCE_GROUP_NAME=<resource-group-name>
az group create \
    --name ${RESOURCE_GROUP_NAME} \
    --location eastus
```

### Creating Azure Storage Account

Run the following commands to create an Azure Storage Account and export its connection string as an environment
variable.

```
STORAGE_ACCOUNT_NAME=<unique-storage-account-name>
az storage account create \
    --name ${STORAGE_ACCOUNT_NAME} \
    --resource-group ${RESOURCE_GROUP_NAME} \
    --location eastus \
    --sku Standard_LRS \
    --kind StorageV2

export QUARKUS_AZURE_STORAGE_BLOB_CONNECTION_STRING=$(az storage account show-connection-string \
    --resource-group ${RESOURCE_GROUP_NAME} \
    --name ${STORAGE_ACCOUNT_NAME} \
    --query connectionString -o tsv)
echo "The value of 'quarkus.azure.storage.blob.connection-string' is: ${QUARKUS_AZURE_STORAGE_BLOB_CONNECTION_STRING}"
```

The value of environment variable `QUARKUS_AZURE_STORAGE_BLOB_CONNECTION_STRING` will be fed into config
property `quarkus.azure.storage.blob.connection-string` of `azure-storage-blob` extension in order to set up the
connection to the Azure Storage Account.

You can also manually copy the output of the variable `quarkus.azure.storage.blob.connection-string` and then
update [application.properties](azure-storage-blob/src/main/resources/application.properties) by uncommenting the
same property and setting copied value.

### Running the test

Finally, build the native executable and launch the test with:

```
mvn integration-test -Dnative -Dquarkus.native.container-build
```

### Cleaning up Azure resources

Once you complete the test, run the following command to clean up the Azure resources used in the test:

```
az group delete \
    --name ${RESOURCE_GROUP_NAME} \
    --yes --no-wait
```
