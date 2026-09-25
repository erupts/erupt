package xyz.erupt.test.util;

import org.junit.jupiter.api.Test;
import xyz.erupt.upms.util.TotpUtil;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the TOTP implementation against the RFC 6238 test vectors, so an
 * authenticator app and erupt always agree on what the current code is.
 */
public class TotpUtilTest {

    // RFC 6238 appendix B seed for HMAC-SHA1, "12345678901234567890" in base32
    private static final String RFC_SECRET =
            TotpUtil.base32Encode("12345678901234567890".getBytes(StandardCharsets.US_ASCII));

    @Test
    public void rfc6238Vectors() {
        // the RFC prints 8 digits; erupt issues the low 6, as every authenticator app does
        assertEquals("287082", TotpUtil.generate(RFC_SECRET, 59L / TotpUtil.PERIOD));
        assertEquals("081804", TotpUtil.generate(RFC_SECRET, 1111111109L / TotpUtil.PERIOD));
        assertEquals("050471", TotpUtil.generate(RFC_SECRET, 1111111111L / TotpUtil.PERIOD));
        assertEquals("005924", TotpUtil.generate(RFC_SECRET, 1234567890L / TotpUtil.PERIOD));
        assertEquals("279037", TotpUtil.generate(RFC_SECRET, 2000000000L / TotpUtil.PERIOD));
    }

    @Test
    public void base32RoundTrip() {
        byte[] raw = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);
        assertEquals("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ", TotpUtil.base32Encode(raw));
        assertEquals(new String(raw, StandardCharsets.US_ASCII),
                new String(TotpUtil.base32Decode("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"), StandardCharsets.US_ASCII));
        // a hand typed secret keeps its grouping and is still decodable
        assertEquals("GEZD GNBV GY3T QOJQ GEZD GNBV GY3T QOJQ",
                TotpUtil.humanize("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"));
        assertEquals(new String(raw, StandardCharsets.US_ASCII),
                new String(TotpUtil.base32Decode("GEZD GNBV GY3T QOJQ GEZD GNBV GY3T QOJQ"), StandardCharsets.US_ASCII));
    }

    @Test
    public void generatedSecretIsUsable() {
        String secret = TotpUtil.generateSecret();
        assertEquals(32, secret.length()); // 160 bit in base32
        assertNotEquals(secret, TotpUtil.generateSecret());
        String code = TotpUtil.generate(secret, TotpUtil.currentCounter());
        assertTrue(TotpUtil.verify(secret, code, 1) >= 0);
        assertTrue(TotpUtil.verify(secret, code, 0) >= 0);
    }

    @Test
    public void verifyRespectsTheWindow() {
        String secret = TotpUtil.generateSecret();
        long counter = TotpUtil.currentCounter();
        assertEquals(counter - 1, TotpUtil.verify(secret, TotpUtil.generate(secret, counter - 1), 1));
        assertEquals(counter + 1, TotpUtil.verify(secret, TotpUtil.generate(secret, counter + 1), 1));
        // two steps out is outside a window of one
        assertEquals(-1, TotpUtil.verify(secret, TotpUtil.generate(secret, counter - 2), 1));
        assertEquals(-1, TotpUtil.verify(secret, "000", 1)); // wrong length
        assertEquals(-1, TotpUtil.verify(secret, null, 1));
    }

    @Test
    public void uriCarriesIssuerAndAccount() {
        String uri = TotpUtil.buildUri("Erupt Demo", "admin@erupt.xyz", RFC_SECRET);
        assertTrue(uri.startsWith("otpauth://totp/Erupt%20Demo:admin%40erupt.xyz?"));
        assertTrue(uri.contains("secret=" + RFC_SECRET));
        assertTrue(uri.contains("issuer=Erupt%20Demo"));
        assertTrue(uri.contains("period=30"));
    }

}
