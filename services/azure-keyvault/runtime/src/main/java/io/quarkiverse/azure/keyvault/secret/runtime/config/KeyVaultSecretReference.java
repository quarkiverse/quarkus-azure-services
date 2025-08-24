package io.quarkiverse.azure.keyvault.secret.runtime.config;

import java.util.Optional;

public record KeyVaultSecretReference(
        String hostAuthority,
        String secretName,
        Optional<String> secretVersion) {
}
