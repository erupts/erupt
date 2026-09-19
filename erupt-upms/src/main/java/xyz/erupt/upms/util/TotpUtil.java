package xyz.erupt.upms.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Time-based one-time password (RFC 6238 / RFC 4226) built on the JDK only.
 * Compatible with Google Authenticator, Microsoft Authenticator, 1Password and every
 * other otpauth:// client: HMAC-SHA1, 6 digits, a 30 second step.
 *
 * @author YuePeng
 * date 2026-09-18
 */
public class TotpUtil {

    public static final int PERIOD = 30;

    private static final int DIGITS = 6;

    private static final int SECRET_BYTES = 20; // 160 bit, the size RFC 4226 recommends for HMAC-SHA1

    private static final char[] BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();

    private static final SecureRandom RANDOM = new SecureRandom();

    private TotpUtil() {
    }

    public static String generateSecret() {
        byte[] buf = new byte[SECRET_BYTES];
        RANDOM.nextBytes(buf);
        return base32Encode(buf);
    }

    /**
     * Build the provisioning URI an authenticator app reads from the QR code.
     * Both the label and the issuer parameter carry the issuer, as the key URI format requires.
     */
    public static String buildUri(String issuer, String account, String secret) {
        return "otpauth://totp/" + urlEncode(issuer) + ":" + urlEncode(account)
                + "?secret=" + secret
                + "&issuer=" + urlEncode(issuer)
                + "&algorithm=SHA1&digits=" + DIGITS + "&period=" + PERIOD;
    }

    /**
     * Verify a code against the counters within {@code window} steps of now.
     *
     * @return the matched counter, or -1 when no counter in the window produces this code
     */
    public static long verify(String secret, String code, int window) {
        if (null == code || null == secret) return -1;
        code = code.trim().replace(" ", "");
        if (code.length() != DIGITS) return -1;
        long counter = currentCounter();
        for (long i = counter - window; i <= counter + window; i++) {
            if (constantTimeEquals(generate(secret, i), code)) return i;
        }
        return -1;
    }

    public static long currentCounter() {
        return System.currentTimeMillis() / 1000 / PERIOD;
    }

    public static String generate(String secret, long counter) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(base32Decode(secret), "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(counter).array());
            // dynamic truncation, RFC 4226 section 5.3
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int mod = 1;
            for (int i = 0; i < DIGITS; i++) mod *= 10;
            return String.format("%0" + DIGITS + "d", binary % mod);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("TOTP generation failed", e);
        }
    }

    /**
     * Group a secret into blocks of four so it can be typed by hand when a camera is unavailable.
     */
    public static String humanize(String secret) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < secret.length(); i++) {
            if (i > 0 && i % 4 == 0) sb.append(' ');
            sb.append(secret.charAt(i));
        }
        return sb.toString();
    }

    public static String base32Encode(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0, bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                sb.append(BASE32[(buffer >> (bitsLeft - 5)) & 0x1F]);
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) sb.append(BASE32[(buffer << (5 - bitsLeft)) & 0x1F]);
        return sb.toString();
    }

    public static byte[] base32Decode(String base32) {
        String value = base32.trim().replace(" ", "").replace("=", "").toUpperCase();
        byte[] out = new byte[value.length() * 5 / 8];
        int buffer = 0, bitsLeft = 0, index = 0;
        for (int i = 0; i < value.length(); i++) {
            int c = indexOfBase32(value.charAt(i));
            if (c < 0) throw new IllegalArgumentException("Illegal base32 character");
            buffer = (buffer << 5) | c;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                out[index++] = (byte) (buffer >> (bitsLeft - 8));
                bitsLeft -= 8;
            }
        }
        return out;
    }

    private static int indexOfBase32(char c) {
        for (int i = 0; i < BASE32.length; i++) {
            if (BASE32[i] == c) return i;
        }
        return -1;
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    private static String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

}
