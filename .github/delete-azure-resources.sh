#!/usr/bin/env bash

# The following environment variables need to be configured before running the script
# - RESOURCE_GROUP_NAME
# - STORAGE_ACCOUNT_NAME
# - APP_CONFIG_NAME
# - KEY_VAULT_NAME

az appconfig delete --name ${APP_CONFIG_NAME} --resource-group ${RESOURCE_GROUP_NAME} --yes
az appconfig purge --name ${APP_CONFIG_NAME} --yes
az storage account delete --name ${STORAGE_ACCOUNT_NAME} --resource-group ${RESOURCE_GROUP_NAME} --yes
az keyvault delete --name ${KEY_VAULT_NAME} --resource-group ${RESOURCE_GROUP_NAME} --yes
az keyvault purge --name ${KEY_VAULT_NAME} --yes
az group delete --name ${RESOURCE_GROUP_NAME} --yes
