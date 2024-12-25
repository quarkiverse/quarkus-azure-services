package io.quarkiverse.azure.core.util;

/**
 * Util class for Azure Quarkus Identifier.
 *
 * @implNote The identifier is used to identify the Azure Quarkus Extension, and its max length is limited to 24 characters.
 * @since 1.0.1
 */
public final class AzureQuarkusIdentifier {

    // length cannot be greater than 24
    public static final String AZURE_QUARKUS_APP_CONFIGURATION = "az-qk-app-config";
    public static final String AZURE_QUARKUS_STORAGE_BLOB = "az-qk-storage-blob";
    public static final String AZURE_QUARKUS_KEY_VAULT_ASYNC_CLIENT = "az-qk-kv-secret-async";
    public static final String AZURE_QUARKUS_KEY_VAULT_SYNC_CLIENT = "az-qk-kv-secret-sync";
    public static final String AZURE_QUARKUS_COSMOS = "az-qk-cosmos";
    public static final String AZURE_QUARKUS_EVENTHUBS = "az-qk-eventhubs";

    private AzureQuarkusIdentifier() {

    }
}