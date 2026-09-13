package xyz.erupt.remote.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * RFB "VNC Authentication" (security type 2) challenge-response.
 * The 16-byte challenge is DES-encrypted with the password, where each key byte has its bits reversed.
 *
 * @author YuePeng
 */
public final class VncAuth {

    private VncAuth() {
    }

    public static byte[] respond(String password, byte[] challenge) throws Exception {
        byte[] pw = password.getBytes(StandardCharsets.ISO_8859_1);
        byte[] key = new byte[8];
        for (int i = 0; i < 8 && i < pw.length; i++) {
            key[i] = (byte) (Integer.reverse(pw[i] & 0xff) >>> 24);
        }
        Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "DES"));
        return cipher.doFinal(challenge);
    }
}
