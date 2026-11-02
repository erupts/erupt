package xyz.erupt.cloud.server.http;

import org.junit.jupiter.api.Test;
import xyz.erupt.cloud.common.http.CloudSignature;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CloudSignatureTest {

    private static final String TOKEN = "node-token";

    private static final long NOW = 1_800_000_000L;

    private static final byte[] BODY = "{\"title\":\"x\"}".getBytes(StandardCharsets.UTF_8);

    private static String sign(long timestamp) {
        return CloudSignature.sign(TOKEN, "POST", "/erupt-api/data/modify/Article", timestamp, "power,record", "dXNlcg==", BODY);
    }

    private static boolean verify(String method, String path, String timestamp, String enforced, String user, byte[] body, String signature) {
        return CloudSignature.verify(TOKEN, method, path, timestamp, enforced, user, body, signature, NOW);
    }

    @Test
    void roundTrip() {
        assertTrue(verify("POST", "/erupt-api/data/modify/Article", Long.toString(NOW), "power,record", "dXNlcg==", BODY, sign(NOW)));
        assertTrue(verify("post", "/erupt-api/data/modify/Article", Long.toString(NOW), "power,record", "dXNlcg==", BODY, sign(NOW)),
                "the method is case insensitive");
    }

    @Test
    void anyAlterationFails() {
        String signature = sign(NOW);
        String now = Long.toString(NOW);
        assertFalse(verify("GET", "/erupt-api/data/modify/Article", now, "power,record", "dXNlcg==", BODY, signature), "method");
        assertFalse(verify("POST", "/erupt-api/data/modify/Note", now, "power,record", "dXNlcg==", BODY, signature), "path");
        assertFalse(verify("POST", "/erupt-api/data/modify/Article", now, "power", "dXNlcg==", BODY, signature), "enforced");
        assertFalse(verify("POST", "/erupt-api/data/modify/Article", now, "power,record", "b3RoZXI=", BODY, signature), "user");
        assertFalse(verify("POST", "/erupt-api/data/modify/Article", now, "power,record", "dXNlcg==", "{}".getBytes(StandardCharsets.UTF_8), signature), "body");
        assertFalse(verify("POST", "/erupt-api/data/modify/Article", now, "power,record", "dXNlcg==", null, signature), "unsigned payload");
        assertFalse(CloudSignature.verify("other-token", "POST", "/erupt-api/data/modify/Article", now, "power,record", "dXNlcg==", BODY, signature, NOW), "key");
    }

    @Test
    void timestampWindow() {
        long old = NOW - CloudSignature.WINDOW_SECONDS - 1;
        assertFalse(verify("POST", "/erupt-api/data/modify/Article", Long.toString(old), "power,record", "dXNlcg==", BODY, sign(old)), "replayed too late");
        long edge = NOW - CloudSignature.WINDOW_SECONDS;
        assertTrue(verify("POST", "/erupt-api/data/modify/Article", Long.toString(edge), "power,record", "dXNlcg==", BODY, sign(edge)), "inside the window");
        assertFalse(verify("POST", "/erupt-api/data/modify/Article", "soon", "power,record", "dXNlcg==", BODY, sign(NOW)), "not a number");
        assertFalse(verify("POST", "/erupt-api/data/modify/Article", null, "power,record", "dXNlcg==", BODY, sign(NOW)), "missing");
    }

    @Test
    void emptyAndMissingBodiesDiffer() {
        assertNotEquals(CloudSignature.sign(TOKEN, "GET", "/p", NOW, null, null, new byte[0]),
                CloudSignature.sign(TOKEN, "GET", "/p", NOW, null, null, null));
    }

}
