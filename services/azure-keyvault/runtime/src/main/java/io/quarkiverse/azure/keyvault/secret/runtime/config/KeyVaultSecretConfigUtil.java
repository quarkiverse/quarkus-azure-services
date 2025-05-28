package io.quarkiverse.azure.keyvault.secret.runtime.config;

import com.azure.security.keyvault.secrets.models.KeyVaultSecretIdentifier;

import io.quarkus.runtime.configuration.ConfigurationException;

public class KeyVaultSecretConfigUtil {
    private static final String AZURE_KEYVAULT_PREFIX = "kv//";
    private static final String AZURE_KEYVAULT_ENDPOINT_PREFIX = "https://";
    private static final String AZURE_VAULT_URL_FORMAT = "https://%s.%s/secrets/%s/%s";
    private static final String AZURE_CLOUD_DNS = "vault.azure.net";

    private KeyVaultSecretConfigUtil() {
    }

    /**
     *
     * @returns a {@link KeyVaultSecretIdentifier} from the specified input or <code>null</code> if the input does not start
     *          with {@link #AZURE_KEYVAULT_PREFIX}.
     *
     * @throws IllegalArgumentException if the input cannot otherwise be parsed.
     *
     */
    static KeyVaultSecretIdentifier getSecretIdentifier(String input, String defaultEndpoint) {

        if (!input.startsWith(AZURE_KEYVAULT_PREFIX)) {
            return null;
        }

        String resourcePath = input.substring(AZURE_KEYVAULT_PREFIX.length());
        String[] tokens = resourcePath.split("/");

        String kvName = null;
        String secretName = null;
        String version = "latest";

        if (tokens.length == 1) {
            // property is form "kv//<secret-name>"
            kvName = getAzureKeyVaultName(defaultEndpoint);
            secretName = tokens[0];
        } else if (tokens.length == 2) {
            // property is form "kv//<kv-name>/<secret-name>"
            kvName = tokens[0];
            secretName = tokens[1];
        } else if (tokens.length == 3
                && tokens[1].equals("secrets")) {
            // property is form "kv//<kv-name>/secrets/<secret-name>"
            kvName = tokens[0];
            secretName = tokens[2];
        } else if (tokens.length == 3
                && tokens[1].equals("versions")) {
            // property is form "kv//<secret-name>}/versions/<version>"
            kvName = getAzureKeyVaultName(defaultEndpoint);
            secretName = tokens[0];
            version = tokens[2];
        } else if (tokens.length == 4
                && tokens[1].equals("secrets")) {
            // property is form "kv//<kv-name>/secrets/<secret-name>/<version>"
            kvName = tokens[0];
            secretName = tokens[2];
            version = tokens[3];
        } else if (tokens.length == 5
                && tokens[1].equals("secrets")
                && tokens[3].equals("versions")) {
            // property is form "kv//<kv-name>/secrets/<secret-name>/versions/<version>"
            kvName = tokens[0];
            secretName = tokens[2];
            version = tokens[4];
        } else {
            throw new IllegalArgumentException(
                    "Unrecognized format for specifying an Azure Key Vault secret: " + input);
        }

        if (kvName.isEmpty() || secretName.isEmpty() || version.isEmpty()) {
            throw new IllegalArgumentException("The provided Key Vault secret URI is invalid: " + input);
        }

        return new KeyVaultSecretIdentifier(
                String.format(AZURE_VAULT_URL_FORMAT, kvName, getKeyValutDNS(defaultEndpoint), secretName, version));
    }

    static String getAzureKeyVaultName(String endpoint) {
        if (endpoint.isEmpty()) {
            throw new ConfigurationException(
                    "The endpoint of Azure Key Vault (quarkus.azure.keyvault.secret.endpoint) should be set.");
        }
        if (!endpoint.startsWith(AZURE_KEYVAULT_ENDPOINT_PREFIX)) {
            throw new ConfigurationException(
                    "The endpoint of Azure Key Vault (quarkus.azure.keyvault.secret.endpoint) should start with https://.");
        }
        return endpoint.substring(AZURE_KEYVAULT_ENDPOINT_PREFIX.length()).split("\\.")[0];
    }

    /**
     * Get the domain name of the key vault from the endpoint.
     *
     * @param endpoint the endpoint of the key vault
     * @return if the endpoint is empty, return "vault.azure.net"; otherwise, return the domain name of the key vault
     *         Relevant documentation:
     *         https://learn.microsoft.com/azure/key-vault/general/about-keys-secrets-certificates#dns-suffixes-for-object-identifiers
     */
    static String getKeyValutDNS(String endpoint) {
        if (endpoint.isEmpty()) {
            // return Azure Cloud DNS suffix
            return AZURE_CLOUD_DNS;
        }

        String kvName = getAzureKeyVaultName(endpoint);
        String dns = endpoint.substring(kvName.length() + AZURE_KEYVAULT_ENDPOINT_PREFIX.length()).split("/")[0];

        return dns.length() > 1 ? dns.substring(1) : AZURE_CLOUD_DNS;
    }
}
