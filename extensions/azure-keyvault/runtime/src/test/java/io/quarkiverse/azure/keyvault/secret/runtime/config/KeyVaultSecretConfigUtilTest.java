package io.quarkiverse.azure.keyvault.secret.runtime.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.azure.security.keyvault.secrets.models.KeyVaultSecretIdentifier;

class KeyVaultSecretConfigUtilTest {

    private static final String DEFAULT_ENDPOINT = "https://contoso.vault.azure.net/";
    private static final String INVALID_ENDPOINT = "http://contoso.vault.azure.net/";
    private static final String VAULT_URL_FORMAT = "https://%s.vault.azure.net";

    @Test
    public void testInvalidEndPoint() {
        assertThrows(AssertionError.class, () -> KeyVaultSecretConfigUtil.getAzureKeyVaultName(INVALID_ENDPOINT),
                "The endpoint of Azure Key Vault should start with https://.");
    }

    @Test
    public void testEmptyEndPoint() {
        assertThrows(AssertionError.class, () -> KeyVaultSecretConfigUtil.getAzureKeyVaultName(""),
                "The endpoint of Azure Key Vault should be set.");
    }

    @Test
    public void testNonSecret() {
        String property = "some.non.secret.property.name";
        KeyVaultSecretIdentifier secretIdentifier = KeyVaultSecretConfigUtil.getSecretIdentifier(property, DEFAULT_ENDPOINT);

        assertThat(secretIdentifier).isNull();
    }

    @Test
    public void testInvalidSecretFormat_missingSecret() {
        String property = "kv//";

        assertThatThrownBy(() -> KeyVaultSecretConfigUtil.getSecretIdentifier(property, DEFAULT_ENDPOINT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testShortProperty_secret() {
        String property = "kv//the-secret";
        KeyVaultSecretIdentifier secretIdentifier = KeyVaultSecretConfigUtil.getSecretIdentifier(property, DEFAULT_ENDPOINT);

        assertThat(secretIdentifier.getVaultUrl()).isEqualTo(String.format(VAULT_URL_FORMAT, "contoso"));
        assertThat(secretIdentifier.getName()).isEqualTo("the-secret");
        assertThat(secretIdentifier.getVersion()).isEqualTo("latest");
    }

    @Test
    public void testShortProperty_secretVersion() {
        String property = "kv//the-secret/versions/the-version";
        KeyVaultSecretIdentifier secretIdentifier = KeyVaultSecretConfigUtil.getSecretIdentifier(property, DEFAULT_ENDPOINT);

        assertThat(secretIdentifier.getVaultUrl()).isEqualTo(String.format(VAULT_URL_FORMAT, "contoso"));
        assertThat(secretIdentifier.getName()).isEqualTo("the-secret");
        assertThat(secretIdentifier.getVersion()).isEqualTo("the-version");
    }

    @Test
    public void testShortProperty_keywordVersionTypo() {
        String property = "kv//the-secret/version/the-version";

        assertThatThrownBy(() -> KeyVaultSecretConfigUtil.getSecretIdentifier(property, DEFAULT_ENDPOINT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testShortProperty_kvSecret() {
        String property = "kv//my-kv/the-secret";
        KeyVaultSecretIdentifier secretIdentifier = KeyVaultSecretConfigUtil.getSecretIdentifier(property, DEFAULT_ENDPOINT);

        assertThat(secretIdentifier.getVaultUrl()).isEqualTo(String.format(VAULT_URL_FORMAT, "my-kv"));
        assertThat(secretIdentifier.getName()).isEqualTo("the-secret");
        assertThat(secretIdentifier.getVersion()).isEqualTo("latest");
    }

    @Test
    public void testLongProperty_kvSecret() {
        String property = "kv//my-kv/secrets/the-secret";
        KeyVaultSecretIdentifier secretIdentifier = KeyVaultSecretConfigUtil.getSecretIdentifier(property, DEFAULT_ENDPOINT);

        assertThat(secretIdentifier.getVaultUrl()).isEqualTo(String.format(VAULT_URL_FORMAT, "my-kv"));
        assertThat(secretIdentifier.getName()).isEqualTo("the-secret");
        assertThat(secretIdentifier.getVersion()).isEqualTo("latest");
    }

    @Test
    public void testLongProperty_keywordSecretTypo() {
        String property = "kv//my-kv/secret/the-secret";

        assertThatThrownBy(() -> KeyVaultSecretConfigUtil.getSecretIdentifier(property, DEFAULT_ENDPOINT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testLongProperty_kvSecret_shortVersion() {
        String property = "kv//my-kv/secrets/the-secret/3";
        KeyVaultSecretIdentifier secretIdentifier = KeyVaultSecretConfigUtil.getSecretIdentifier(property, DEFAULT_ENDPOINT);

        assertThat(secretIdentifier.getVaultUrl()).isEqualTo(String.format(VAULT_URL_FORMAT, "my-kv"));
        assertThat(secretIdentifier.getName()).isEqualTo("the-secret");
        assertThat(secretIdentifier.getVersion()).isEqualTo("3");
    }

    @Test
    public void testLongProperty_kvSecret_shortVersionTypo() {
        String property = "kv//my-kv/secret/the-secret/3";

        assertThatThrownBy(() -> KeyVaultSecretConfigUtil.getSecretIdentifier(property, DEFAULT_ENDPOINT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testLongProperty_kvSecret_LongVersion() {
        String property = "kv//my-kv/secrets/the-secret/versions/3";
        KeyVaultSecretIdentifier secretIdentifier = KeyVaultSecretConfigUtil.getSecretIdentifier(property, DEFAULT_ENDPOINT);

        assertThat(secretIdentifier.getVaultUrl()).isEqualTo(String.format(VAULT_URL_FORMAT, "my-kv"));
        assertThat(secretIdentifier.getName()).isEqualTo("the-secret");
        assertThat(secretIdentifier.getVersion()).isEqualTo("3");
    }

    @Test
    public void testLongProperty_kvSecret_LongVersionTypo() {
        String property = "kv//my-kv/secrets/the-secret/version/3";

        assertThatThrownBy(() -> KeyVaultSecretConfigUtil.getSecretIdentifier(property, DEFAULT_ENDPOINT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testGetKeyVaultDNS() {
        String publicCloudDNS = "https://my-kv.vault.azure.net/";
        String dns = KeyVaultSecretConfigUtil.getKeyValutDNS(publicCloudDNS);
        assertThat(dns).isEqualTo("vault.azure.net");

        String governmentCloudDNS = "https://my-kv.vault.usgovcloudapi.net";
        dns = KeyVaultSecretConfigUtil.getKeyValutDNS(governmentCloudDNS);
        assertThat(dns).isEqualTo("vault.usgovcloudapi.net");

        String chinaCloudDNS = "https://my-kv.vault.azure.cn";
        dns = KeyVaultSecretConfigUtil.getKeyValutDNS(chinaCloudDNS);
        assertThat(dns).isEqualTo("vault.azure.cn");

        String germanyCloudDNS = "https://my-kv.vault.microsoftazure.de";
        dns = KeyVaultSecretConfigUtil.getKeyValutDNS(germanyCloudDNS);
        assertThat(dns).isEqualTo("vault.microsoftazure.de");

        String publicCloudDNSWithSubDomain = "https://my-kv.subdomain.vault.azure.net";
        dns = KeyVaultSecretConfigUtil.getKeyValutDNS(publicCloudDNSWithSubDomain);
        assertThat(dns).isEqualTo("subdomain.vault.azure.net");

        String invalidDNS = "https://my-kv";
        dns = KeyVaultSecretConfigUtil.getKeyValutDNS(invalidDNS);
        assertThat(dns).isEqualTo("vault.azure.net");

        String invalidUri = "https://";
        dns = KeyVaultSecretConfigUtil.getKeyValutDNS(invalidUri);
        assertThat(dns).isEqualTo("vault.azure.net");
    }
}
