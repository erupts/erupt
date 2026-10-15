package xyz.erupt.cloud.common.http;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * HMAC-SHA256 signature of a server -> node request, keyed by the node's access token, so the token itself
 * never travels and the user header and body cannot be altered or replayed outside a five-minute window.
 * <pre>
 * method \n path?query \n timestamp \n enforced \n sha256(user header) \n sha256(body) | UNSIGNED-PAYLOAD
 * </pre>
 * A multipart body is re-encoded by the forwarding client, so it is not covered ({@code null} body).
 *
 * @author YuePeng
 * date 2026/10/14
 */
public final class CloudSignature {

    public static final String HEADER_TIMESTAMP = "Erupt-Timestamp";

    public static final String HEADER_SIGNATURE = "Erupt-Signature";

    // Checks the server ran before forwarding, so the node may skip them: see Enforced
    public static final String HEADER_ENFORCED = "Erupt-Enforced";

    public static final long WINDOW_SECONDS = 300;

    private static final String UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD";

    private static final String HMAC = "HmacSHA256";

    private static final String DIGEST = "SHA-256";

    private CloudSignature() {
    }

    public static String sign(String accessToken, String method, String pathAndQuery, long timestamp,
                              String enforced, String userHeader, byte[] body) {
        String canonical = method.toUpperCase() + "\n" + pathAndQuery + "\n" + timestamp + "\n"
                + (null == enforced ? "" : enforced) + "\n"
                + sha256(null == userHeader ? new byte[0] : userHeader.getBytes(StandardCharsets.UTF_8)) + "\n"
                + (null == body ? UNSIGNED_PAYLOAD : sha256(body));
        try {
            Mac mac = Mac.getInstance(HMAC);
            mac.init(new SecretKeySpec(accessToken.getBytes(StandardCharsets.UTF_8), HMAC));
            return HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param timestampHeader the {@link #HEADER_TIMESTAMP} value as received
     * @param nowSeconds      the verifier's clock, epoch seconds
     */
    public static boolean verify(String accessToken, String method, String pathAndQuery, String timestampHeader,
                                 String enforced, String userHeader, byte[] body, String signature, long nowSeconds) {
        if (null == accessToken || null == timestampHeader || null == signature) return false;
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampHeader.trim());
        } catch (NumberFormatException e) {
            return false;
        }
        if (Math.abs(nowSeconds - timestamp) > WINDOW_SECONDS) return false;
        String expected = sign(accessToken, method, pathAndQuery, timestamp, enforced, userHeader, body);
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.trim().getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance(DIGEST).digest(data));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Values of {@link #HEADER_ENFORCED}, comma separated.
     */
    public enum Enforced {
        // the user's rights for the endpoint (EruptRouter.power) and the build model's power
        POWER,
        // search conditions, sort and paging of a TableQuery
        QUERY,
        // not-null, number, length, choice and XSS rules of a submitted record
        RECORD;

        // The header value: the checks in declaration order, lower case, comma separated
        public static String header(Set<Enforced> enforced) {
            return enforced.stream().sorted().map(it -> it.name().toLowerCase()).collect(Collectors.joining(","));
        }
    }

}
