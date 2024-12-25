#!/usr/bin/env bash

# The following environment variables need to be configured before running the script
# - RESOURCE_GROUP_NAME
# - APP_CONFIG_NAME
# - KEY_VAULT_NAME

az appconfig delete --name "${APP_CONFIG_NAME}" --resource-group "${RESOURCE_GROUP_NAME}" --yes
az appconfig purge --name "${APP_CONFIG_NAME}" --yes
az keyvault delete --name "${KEY_VAULT_NAME}" --resource-group "${RESOURCE_GROUP_NAME}"
az keyvault purge --name "${KEY_VAULT_NAME}"
az eventhubs namespace delete --resource-group "${RESOURCE_GROUP_NAME}" --name "${EVENTHUBS_NAMESPACE}"
az group delete --name "${RESOURCE_GROUP_NAME}" --yes --no-wait
