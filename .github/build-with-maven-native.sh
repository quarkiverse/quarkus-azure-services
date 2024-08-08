#!/usr/bin/env bash
set -Euo pipefail

# The following environment variables need to be configured before running the script
# - RESOURCE_GROUP_NAME
# - STORAGE_ACCOUNT_NAME
# - APP_CONFIG_NAME
# - KEY_VAULT_NAME

# Create a resource group
az group create \
    --name "${RESOURCE_GROUP_NAME}" \
    --location centralus

# Azure Storage Blob Extension
# The same commands used in
#  - integration-tests/README.md
#  - integration-tests/azure-storage-blob/README.md

az storage account create \
    --name "${STORAGE_ACCOUNT_NAME}" \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    --location centralus \
    --sku Standard_LRS \
    --kind StorageV2

export QUARKUS_AZURE_STORAGE_BLOB_CONNECTION_STRING=$(az storage account show-connection-string \
  --resource-group "${RESOURCE_GROUP_NAME}" \
  --name "${STORAGE_ACCOUNT_NAME}" \
  --query connectionString -o tsv)

# Azure App Configuration Extension
# The same commands used in
#  - integration-tests/README.md
#  - integration-tests/azure-app-configuration/README.md

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

# Azure Key Vault Extension
# The same commands used in 
#  - integration-tests/README.md
#  - integration-tests/azure-keyvault/README.md

az keyvault create \
    --name "${KEY_VAULT_NAME}" \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    --location eastus \
    --enable-rbac-authorization false

az keyvault secret set \
    --vault-name "${KEY_VAULT_NAME}" \
    --name secret1 \
    --value mysecret

export QUARKUS_AZURE_KEYVAULT_SECRET_ENDPOINT=$(az keyvault show --name "${KEY_VAULT_NAME}" \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    --query properties.vaultUri\
    --output tsv)

# Build native executable and run the integration tests against the Azure services
mvn -B install -Dnative -Dquarkus.native.container-build -Dnative.surefire.skip -Dazure.test=true
