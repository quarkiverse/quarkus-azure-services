#!/usr/bin/env bash
set -Euo pipefail

# The following environment variables need to be configured before running the script
# - RESOURCE_GROUP_NAME
# - STORAGE_ACCOUNT_NAME
# - APP_CONFIG_NAME
# - KEY_VAULT_NAME
# - COSMOSDB_ACCOUNT_NAME

# Azure Storage Blob Extension
export QUARKUS_AZURE_STORAGE_BLOB_CONNECTION_STRING=$(az storage account show-connection-string \
  --resource-group "${RESOURCE_GROUP_NAME}" \
  --name "${STORAGE_ACCOUNT_NAME}" \
  --query connectionString -o tsv)

# Azure App Configuration Extension
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

# Azure Key Vault Extension
export QUARKUS_AZURE_KEYVAULT_SECRET_ENDPOINT=$(az keyvault show --name "${KEY_VAULT_NAME}" \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    --query properties.vaultUri\
    --output tsv)

# Azure Cosmos Extension
export QUARKUS_AZURE_COSMOS_ENDPOINT=$(az cosmosdb show \
    -n ${COSMOSDB_ACCOUNT_NAME} \
    -g ${RESOURCE_GROUP_NAME} \
    --query documentEndpoint -o tsv)

# Randomly authenticate to Azure Cosmos DB with key or data plane RBAC
number=$(shuf -i 1-100 -n 1)
if [ $((number % 2)) -eq 0 ]; then
  # Export the key that has full access to the account including management plane and data plane operations
  export QUARKUS_AZURE_COSMOS_KEY=$(az cosmosdb keys list \
      -n ${COSMOSDB_ACCOUNT_NAME} \
      -g ${RESOURCE_GROUP_NAME} \
      --query primaryMasterKey -o tsv)
else
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

  servicePrincipal=$(az ad sp list --filter "appId eq '$AZURE_CLIENT_ID'" --query '[0].id' -o tsv)
  az cosmosdb sql role assignment create \
      --account-name ${COSMOSDB_ACCOUNT_NAME} \
      --resource-group ${RESOURCE_GROUP_NAME} \
      --scope "/" \
      --principal-id ${servicePrincipal} \
      --role-definition-id 00000000-0000-0000-0000-000000000002
fi

# Run integration test with existing native executables against Azure services
mvn -B test-compile failsafe:integration-test -Dnative -Dazure.test=true

# Run both unit test and integration test in JVM mode against Azure services
mvn -B verify -Dazure.test=true
