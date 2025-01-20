#!/usr/bin/env bash
set -Eeuo pipefail

# The following environment variables need to be configured before running the script
# - RESOURCE_GROUP_NAME
# - STORAGE_ACCOUNT_NAME
# - APP_CONFIG_NAME
# - KEY_VAULT_NAME
# - COSMOSDB_ACCOUNT_NAME
# - EVENTHUBS_NAMESPACE
# - EVENTHUBS_EVENTHUB_NAME

# Test azure-services-disabled
mvn -f azure-services-disabled/pom.xml test-compile failsafe:integration-test failsafe:verify -Dnative -Dazure.test=true
mvn -f azure-services-disabled/pom.xml verify -Dazure.test=true

# Azure Storage Blob Extension
# Authenticate to Azure Storage Blob with Microsoft Entra ID and connection string
# Get the endpoint of azure storage blob
AZURE_STORAGE_BLOB_ENDPOINT=$(az storage account show \
    --resource-group $RESOURCE_GROUP_NAME \
    --name $STORAGE_ACCOUNT_NAME \
    --query 'primaryEndpoints.blob' \
    --output tsv)
# Retrieve the storage account resource ID
STORAGE_ACCOUNT_RESOURCE_ID=$(az storage account show \
    --resource-group $RESOURCE_GROUP_NAME \
    --name $STORAGE_ACCOUNT_NAME \
    --query 'id' \
    --output tsv)
# Assign the "Storage Blob Data Contributor" role to the current signed-in identity
OBJECT_ID=$(az ad sp list --filter "appId eq '$AZURE_CLIENT_ID'" --query '[0].id' -o tsv)
az role assignment create \
    --assignee ${OBJECT_ID} \
    --role "Storage Blob Data Contributor" \
    --scope $STORAGE_ACCOUNT_RESOURCE_ID
# Get the connection string that has full access to the account
AZURE_STORAGE_BLOB_CONNECTION_STRING=$(az storage account show-connection-string \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    --name "${STORAGE_ACCOUNT_NAME}" \
    --query connectionString -o tsv)
# Run integration test with existing native executables against Azure services
mvn -f azure-storage-blob/pom.xml test-compile failsafe:integration-test failsafe:verify -Dnative -Dazure.test=true -Dquarkus.azure.storage.blob.endpoint=${AZURE_STORAGE_BLOB_ENDPOINT}
mvn -f azure-storage-blob/pom.xml test-compile failsafe:integration-test failsafe:verify -Dnative -Dazure.test=true -Dquarkus.azure.storage.blob.connection-string=${AZURE_STORAGE_BLOB_CONNECTION_STRING}
# Run both unit test and integration test in JVM mode against Azure services
mvn -f azure-storage-blob/pom.xml verify -Dazure.test=true -Dquarkus.azure.storage.blob.endpoint=${AZURE_STORAGE_BLOB_ENDPOINT}
mvn -f azure-storage-blob/pom.xml verify -Dazure.test=true -Dquarkus.azure.storage.blob.connection-string=${AZURE_STORAGE_BLOB_CONNECTION_STRING}

# Azure App Configuration Extension
# Authenticate to Azure App Configuration with Microsoft Entra ID and access keys
# Export the endpoint of azure app configuration
export QUARKUS_AZURE_APP_CONFIGURATION_ENDPOINT=$(az appconfig show \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    --name "${APP_CONFIG_NAME}" \
    --query endpoint -o tsv)
# Retrieve the app configuration resource ID
APP_CONFIGURATION_RESOURCE_ID=$(az appconfig show \
    --resource-group $RESOURCE_GROUP_NAME \
    --name "${APP_CONFIG_NAME}" \
    --query 'id' \
    --output tsv)
# Assign the "App Configuration Data Reader" role to the current signed-in identity
az role assignment create \
    --assignee ${OBJECT_ID} \
    --role "App Configuration Data Reader" \
    --scope $APP_CONFIGURATION_RESOURCE_ID
# Get the access keys (id and secret) that has full access to the app configuration store
credential=$(az appconfig credential list \
    --name "${APP_CONFIG_NAME}" \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    | jq 'map(select(.readOnly == true)) | .[0]')
AZURE_APP_CONFIGURATION_ID=$(echo "${credential}" | jq -r '.id')
AZURE_APP_CONFIGURATION_SECRET=$(echo "${credential}" | jq -r '.value')
mvn -f azure-app-configuration/pom.xml test-compile failsafe:integration-test failsafe:verify -Dnative -Dazure.test=true
mvn -f azure-app-configuration/pom.xml test-compile failsafe:integration-test failsafe:verify -Dnative -Dazure.test=true \
    -Dquarkus.azure.app.configuration.id=${AZURE_APP_CONFIGURATION_ID} -Dquarkus.azure.app.configuration.secret=${AZURE_APP_CONFIGURATION_SECRET}
mvn -f azure-app-configuration/pom.xml verify -Dazure.test=true
mvn -f azure-app-configuration/pom.xml verify -Dazure.test=true \
    -Dquarkus.azure.app.configuration.id=${AZURE_APP_CONFIGURATION_ID} -Dquarkus.azure.app.configuration.secret=${AZURE_APP_CONFIGURATION_SECRET}

# Azure Key Vault Extension
export QUARKUS_AZURE_KEYVAULT_SECRET_ENDPOINT=$(az keyvault show --name "${KEY_VAULT_NAME}" \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    --query properties.vaultUri\
    --output tsv)
mvn -f azure-keyvault/pom.xml test-compile failsafe:integration-test failsafe:verify -Dnative -Dazure.test=true
mvn -f azure-keyvault/pom.xml verify -Dazure.test=true

# Azure Event Hubs Extension
# Retrieve the event hub resource ID
EVENTHUBS_EVENTHUB_RESOURCE_ID=$(az eventhubs eventhub show \
    --resource-group $RESOURCE_GROUP_NAME \
    --namespace-name $EVENTHUBS_NAMESPACE \
    --name $EVENTHUBS_EVENTHUB_NAME \
    --query 'id' \
    --output tsv)
# Assign the "Azure Event Hubs Data Owner" role to the current signed-in identity
az role assignment create \
    --role "Azure Event Hubs Data Owner" \
    --assignee ${OBJECT_ID} \
    --scope $EVENTHUBS_EVENTHUB_RESOURCE_ID
export QUARKUS_AZURE_EVENTHUBS_NAMESPACE=${EVENTHUBS_NAMESPACE}
export QUARKUS_AZURE_EVENTHUBS_EVENTHUB_NAME=${EVENTHUBS_EVENTHUB_NAME}
mvn -f azure-eventhubs/pom.xml test-compile failsafe:integration-test failsafe:verify -Dnative -Dazure.test=true
mvn -f azure-eventhubs/pom.xml verify -Dazure.test=true

# Azure Cosmos Extension
# Authenticate to Azure Cosmos DB with Microsoft Entra ID and key
# Export the endpoint of azure cosmos db
export QUARKUS_AZURE_COSMOS_ENDPOINT=$(az cosmosdb show \
    -n ${COSMOSDB_ACCOUNT_NAME} \
    -g ${RESOURCE_GROUP_NAME} \
    --query documentEndpoint -o tsv)
# Create a database and a container beforehand as data plane operations with assigned role cannot create them
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
# Assign the "Cosmos DB Data Contributor" role to the current signed-in identity
az cosmosdb sql role assignment create \
    --account-name ${COSMOSDB_ACCOUNT_NAME} \
    --resource-group ${RESOURCE_GROUP_NAME} \
    --scope "/" \
    --principal-id ${OBJECT_ID} \
    --role-definition-id 00000000-0000-0000-0000-000000000002
# Get the key that has full access to the account including management plane and data plane operations
AZURE_COSMOS_KEY=$(az cosmosdb keys list \
    -n ${COSMOSDB_ACCOUNT_NAME} \
    -g ${RESOURCE_GROUP_NAME} \
    --query primaryMasterKey -o tsv)
mvn -f azure-cosmos/pom.xml test-compile failsafe:integration-test failsafe:verify -Dnative -Dazure.test=true
mvn -f azure-cosmos/pom.xml test-compile failsafe:integration-test failsafe:verify -Dnative -Dazure.test=true -Dquarkus.azure.cosmos.key=${AZURE_COSMOS_KEY}
mvn -f azure-cosmos/pom.xml verify -Dazure.test=true
mvn -f azure-cosmos/pom.xml verify -Dazure.test=true -Dquarkus.azure.cosmos.key=${AZURE_COSMOS_KEY}

# Test azure-services-together
# QUARKUS_AZURE_KEYVAULT_SECRET_ENDPOINT is required as an environment variable but already set in the previous test
# Set connection string of Azure Storage Blob as a secret in Azure Key Vault
az keyvault secret set \
    --vault-name ${KEY_VAULT_NAME} \
    --name secret-azure-storage-blob-conn-string \
    --value "$AZURE_STORAGE_BLOB_CONNECTION_STRING"
# Retrieve the connection string of Azure Storage Blob from Azure Key Vault as a configuration property for azure-storage-blob extension
export QUARKUS_AZURE_STORAGE_BLOB_CONNECTION_STRING=\${kv//secret-azure-storage-blob-conn-string}
export QUARKUS_AZURE_KEYVAULT_SECRET_ENABLED=true
export QUARKUS_AZURE_STORAGE_BLOB_ENABLED=true
mvn -f azure-services-together/pom.xml test-compile failsafe:integration-test failsafe:verify -Dnative -Dazure.test=true
mvn -f azure-services-together/pom.xml verify -Dazure.test=true
