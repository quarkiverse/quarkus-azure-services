#!/usr/bin/env bash
set -Eeuo pipefail

# The following environment variables need to be configured before running the script
# - RESOURCE_GROUP_NAME
# - STORAGE_ACCOUNT_NAME
# - APP_CONFIG_NAME

# Create a resource group
az group create \
    --name "${RESOURCE_GROUP_NAME}" \
    --location eastus

# Create a storage account
az storage account create \
    --name "${STORAGE_ACCOUNT_NAME}" \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    --location eastus \
    --sku Standard_LRS \
    --kind StorageV2

# Retrieve the connection string for the storage account and export as an environment variable
export QUARKUS_AZURE_STORAGE_BLOB_CONNECTION_STRING=$(az storage account show-connection-string \
  --resource-group "${RESOURCE_GROUP_NAME}" \
  --name "${STORAGE_ACCOUNT_NAME}" \
  --query connectionString -o tsv)

# Create an app configuration store
az appconfig create \
    --name "${APP_CONFIG_NAME}" \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    --location eastus

# Add a few key-value pairs to the app configuration store
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

# Retrieve the connection info for the app configuration store and export as environment variables
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

# Build native executable and run the integration tests against the Azure services
mvn -B install -Dnative -Dquarkus.native.container-build -Dnative.surefire.skip -Dazure.test=true

# Delete the resource group
az group delete \
    --name "${RESOURCE_GROUP_NAME}" \
    --yes --no-wait
