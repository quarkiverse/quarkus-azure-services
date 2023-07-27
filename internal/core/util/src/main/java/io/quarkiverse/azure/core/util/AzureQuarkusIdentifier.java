package io.quarkiverse.azure.core.util;

/**
 * Util class for Azure Quarkus Identifier.
 *
 * @implNote The identifier is used to identify the Azure Quarkus Extension, and its max length is limited to 24 characters.
 * @since 1.0.1
 */
public final class AzureQuarkusIdentifier {

    public static final String AZURE_QUARKUS_APP_CONFIGURATION = "az-qk-app-config";
    public static final String AZURE_QUARKUS_STORAGE_BLOB = "az-qk-storage-blob";

    private AzureQuarkusIdentifier() {

    }
}