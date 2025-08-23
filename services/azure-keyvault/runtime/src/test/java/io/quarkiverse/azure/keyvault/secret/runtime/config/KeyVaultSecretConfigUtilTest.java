package io.quarkiverse.azure.keyvault.secret.runtime.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.quarkus.runtime.configuration.ConfigurationException;

class KeyVaultSecretConfigUtilTest {

    private static final String DEFAULT_ENDPOINT = "https://contoso.vault.azure.net/";
    private static final String INVALID_ENDPOINT = "http://contoso.vault.azure.net/";
    private static final String VAULT_URL_FORMAT = "%s.vault.azure.net";

    @Test
    public void testInvalidEndPoint() {
        assertThrows(ConfigurationException.class, () -> KeyVaultSecretConfigUtil.getAzureKeyVaultName(INVALID_ENDPOINT),
                "The endpoint of Azure Key Vault should start with https://.");
    }

    @Test
    public void testEmptyEndPoint() {
        assertThrows(ConfigurationException.class, () -> KeyVaultSecretConfigUtil.getAzureKeyVaultName(""),
                "The endpoint of Azure Key Vault should be set.");
    }

    @Test
    public void testNonSecret() {
        String property = "some.non.secret.property.name";
        KeyVaultSecretReference reference = KeyVaultSecretConfigUtil.getSecretReference(property, DEFAULT_ENDPOINT);

        assertThat(reference).isNull();
    }

    @Test
    public void testInvalidSecretFormat_missingSecret() {
        String property = "kv//";

        assertThatThrownBy(() -> KeyVaultSecretConfigUtil.getSecretReference(property, DEFAULT_ENDPOINT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testShortProperty_secret() {
        String property = "kv//the-secret";
        KeyVaultSecretReference reference = KeyVaultSecretConfigUtil.getSecretReference(property, DEFAULT_ENDPOINT);

        assertThat(reference.hostAuthority()).isEqualTo(String.format(VAULT_URL_FORMAT, "contoso"));
        assertThat(reference.secretName()).isEqualTo("the-secret");
        assertThat(reference.secretVersion()).isEmpty();
    }

    @Test
    public void testShortProperty_secretVersion() {
        String property = "kv//the-secret/versions/the-version";
        KeyVaultSecretReference reference = KeyVaultSecretConfigUtil.getSecretReference(property, DEFAULT_ENDPOINT);

        assertThat(reference.hostAuthority()).isEqualTo(String.format(VAULT_URL_FORMAT, "contoso"));
        assertThat(reference.secretName()).isEqualTo("the-secret");
        assertThat(reference.secretVersion()).hasValue("the-version");
    }

    @Test
    public void testShortProperty_keywordVersionTypo() {
        String property = "kv//the-secret/version/the-version";

        assertThatThrownBy(() -> KeyVaultSecretConfigUtil.getSecretReference(property, DEFAULT_ENDPOINT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testShortProperty_kvSecret() {
        String property = "kv//my-kv/the-secret";
        KeyVaultSecretReference reference = KeyVaultSecretConfigUtil.getSecretReference(property, DEFAULT_ENDPOINT);

        assertThat(reference.hostAuthority()).isEqualTo(String.format(VAULT_URL_FORMAT, "my-kv"));
        assertThat(reference.secretName()).isEqualTo("the-secret");
        assertThat(reference.secretVersion()).isEmpty();
    }

    @Test
    public void testLongProperty_kvSecret() {
        String property = "kv//my-kv/secrets/the-secret";
        KeyVaultSecretReference reference = KeyVaultSecretConfigUtil.getSecretReference(property, DEFAULT_ENDPOINT);

        assertThat(reference.hostAuthority()).isEqualTo(String.format(VAULT_URL_FORMAT, "my-kv"));
        assertThat(reference.secretName()).isEqualTo("the-secret");
        assertThat(reference.secretVersion()).isEmpty();
    }

    @Test
    public void testLongProperty_keywordSecretTypo() {
        String property = "kv//my-kv/secret/the-secret";

        assertThatThrownBy(() -> KeyVaultSecretConfigUtil.getSecretReference(property, DEFAULT_ENDPOINT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testLongProperty_kvSecret_shortVersion() {
        String property = "kv//my-kv/secrets/the-secret/3";
        KeyVaultSecretReference reference = KeyVaultSecretConfigUtil.getSecretReference(property, DEFAULT_ENDPOINT);

        assertThat(reference.hostAuthority()).isEqualTo(String.format(VAULT_URL_FORMAT, "my-kv"));
        assertThat(reference.secretName()).isEqualTo("the-secret");
        assertThat(reference.secretVersion()).hasValue("3");
    }

    @Test
    public void testLongProperty_kvSecret_shortVersionTypo() {
        String property = "kv//my-kv/secret/the-secret/3";

        assertThatThrownBy(() -> KeyVaultSecretConfigUtil.getSecretReference(property, DEFAULT_ENDPOINT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testLongProperty_kvSecret_LongVersion() {
        String property = "kv//my-kv/secrets/the-secret/versions/3";
        KeyVaultSecretReference reference = KeyVaultSecretConfigUtil.getSecretReference(property, DEFAULT_ENDPOINT);

        assertThat(reference.hostAuthority()).isEqualTo(String.format(VAULT_URL_FORMAT, "my-kv"));
        assertThat(reference.secretName()).isEqualTo("the-secret");
        assertThat(reference.secretVersion()).hasValue("3");
    }

    @Test
    public void testLongProperty_kvSecret_LongVersionTypo() {
        String property = "kv//my-kv/secrets/the-secret/version/3";

        assertThatThrownBy(() -> KeyVaultSecretConfigUtil.getSecretReference(property, DEFAULT_ENDPOINT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testGetKeyVaultDNS() {
        String publicCloudDNS = "https://my-kv.vault.azure.net/";
        String dns = KeyVaultSecretConfigUtil.getKeyVaultDns(publicCloudDNS);
        assertThat(dns).isEqualTo("vault.azure.net");

        String governmentCloudDNS = "https://my-kv.vault.usgovcloudapi.net";
        dns = KeyVaultSecretConfigUtil.getKeyVaultDns(governmentCloudDNS);
        assertThat(dns).isEqualTo("vault.usgovcloudapi.net");

        String chinaCloudDNS = "https://my-kv.vault.azure.cn";
        dns = KeyVaultSecretConfigUtil.getKeyVaultDns(chinaCloudDNS);
        assertThat(dns).isEqualTo("vault.azure.cn");

        String germanyCloudDNS = "https://my-kv.vault.microsoftazure.de";
        dns = KeyVaultSecretConfigUtil.getKeyVaultDns(germanyCloudDNS);
        assertThat(dns).isEqualTo("vault.microsoftazure.de");

        String publicCloudDNSWithSubDomain = "https://my-kv.subdomain.vault.azure.net";
        dns = KeyVaultSecretConfigUtil.getKeyVaultDns(publicCloudDNSWithSubDomain);
        assertThat(dns).isEqualTo("subdomain.vault.azure.net");

        String invalidDNS = "https://my-kv";
        dns = KeyVaultSecretConfigUtil.getKeyVaultDns(invalidDNS);
        assertThat(dns).isEqualTo("vault.azure.net");

        String invalidUri = "https://";
        dns = KeyVaultSecretConfigUtil.getKeyVaultDns(invalidUri);
        assertThat(dns).isEqualTo("vault.azure.net");
    }
}
