#!/usr/bin/env bash

# The following environment variables need to be configured before running the script
# - RESOURCE_GROUP_NAME

az group delete --name "${RESOURCE_GROUP_NAME}" --yes --no-wait
