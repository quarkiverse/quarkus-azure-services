#!/usr/bin/env bash
set -Eeuo pipefail

# The following environment variables need to be configured before running the script
# - RESOURCE_GROUP_NAME
# - STORAGE_ACCOUNT_NAME

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

# Retrieve the connection string for the storage account and export it as an environment variable
export QUARKUS_AZURE_STORAGE_BLOB_CONNECTION_STRING=$(az storage account show-connection-string \
  --resource-group "${RESOURCE_GROUP_NAME}" \
  --name "${STORAGE_ACCOUNT_NAME}" \
  --query connectionString -o tsv)

# Build native executable and run the integration tests against the Azure services
mvn -B install -Dnative -Dquarkus.native.container-build

# Delete the resource group
az group delete \
    --name "${RESOURCE_GROUP_NAME}" \
    --yes --no-wait
