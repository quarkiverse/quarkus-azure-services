#!/usr/bin/env bash
set -Euo pipefail

# The following environment variables need to be configured before running the script
# - RESOURCE_GROUP_NAME
# - STORAGE_ACCOUNT_NAME
# - APP_CONFIG_NAME
# - KEY_VAULT_NAME
# - COSMOSDB_ACCOUNT_NAME

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

# Azure Cosmos Extension
# The same commands used in 
#  - integration-tests/README.md
#  - integration-tests/azure-cosmos/README.md
az cosmosdb create \
    -n ${COSMOSDB_ACCOUNT_NAME} \
    -g ${RESOURCE_GROUP_NAME} \
    --default-consistency-level Session \
    --locations regionName='West US' failoverPriority=0 isZoneRedundant=False

# Azure Eventhubs Extension
az eventhubs namespace create \
    --name ${EVENTHUB_NAMESPACE_NAME} \
    --resource-group ${RESOURCE_GROUP_NAME}

az eventhubs eventhub create \
    --name ${EVENTHUB_NAME} \
    --namespace-name ${EVENTHUB_NAMESPACE_NAME} \
    --resource-group ${RESOURCE_GROUP_NAME} \
    --partition-count 2
