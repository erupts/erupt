package xyz.erupt.test.util;

import org.junit.jupiter.api.Test;
import xyz.erupt.core.util.SecretUtil;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests for {@link SecretUtil} Base64 encode / decode round-trips.
 */
public class SecretUtilTest {

    @Test
    public void encodeSecretRoundTrip() {
        String raw = "hello";
        String enc = SecretUtil.encodeSecret(raw, 1);
        // encoded form must differ from the original
        assertNotEquals(raw, enc, "encoded value should differ from raw");
        assertEquals(raw, SecretUtil.decodeSecret(enc), "single-pass round-trip must restore original");
    }

    @Test
    public void encodeSecretMultiPassRoundTrip() {
        String raw = "erupt-secret-123";
        int times = 3;
        String enc = SecretUtil.encodeSecret(raw, times);
        assertEquals(raw, SecretUtil.decodeSecret(enc, times), "multi-pass round-trip must restore original");
    }

    @Test
    public void encodeSecretIsBase64() {
        String raw = "abc";
        String enc = SecretUtil.encodeSecret(raw, 1);
        assertEquals(raw, new String(Base64.getDecoder().decode(enc)), "single pass must be plain Base64");
    }

    @Test
    public void encodeMismatchedPassDoesNotRestore() {
        String raw = "data";
        String enc = SecretUtil.encodeSecret(raw, 2);
        // decoding with the wrong pass count must not yield the original
        assertNotEquals(raw, SecretUtil.decodeSecret(enc, 1), "mismatched pass count should not restore original");
    }
}
