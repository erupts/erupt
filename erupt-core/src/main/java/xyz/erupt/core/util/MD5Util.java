package xyz.erupt.core.util;

import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * @author YuePeng
 * date 2018-10-10.
 */
public class MD5Util {

    private final static String[] STR_DIGITS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    private static String byteToArrayString(byte bByte) {
        int iRet = bByte;
        if (iRet < 0) {
            iRet += 256;
        }
        int iD1 = iRet / 16;
        int iD2 = iRet % 16;
        return STR_DIGITS[iD1] + STR_DIGITS[iD2];
    }

    private static String byteToString(byte[] bByte) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bByte) {
            sb.append(byteToArrayString(b));
        }
        return sb.toString();
    }

    public static String digest(String strObj) {
        return digest(strObj, StandardCharsets.UTF_8.name());
    }

    public static String digest(String strObj, String charset) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return byteToString(md.digest(strObj.getBytes(charset)));
        } catch (Exception ex) {
            return strObj;
        }
    }

    // str -> md5str -> md5(md5str + salt )-> md5salt
    public static String digestSalt(String strObj, String salt) {
        strObj = digest(strObj);
        strObj += salt;
        return digest(strObj);
    }

    /**
     * SHA-512 hash.
     *
     * @param strObj input string
     * @return hex-encoded SHA-512 digest
     */
    public static String digestSHA512(String strObj) {
        return digestSHA512(strObj, StandardCharsets.UTF_8.name());
    }

    /**
     * SHA-512 hash with explicit charset.
     *
     * @param strObj  input string
     * @param charset character encoding
     * @return hex-encoded SHA-512 digest
     */
    @SneakyThrows
    public static String digestSHA512(String strObj, String charset) {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        return byteToString(md.digest(strObj.getBytes(charset)));
    }

    /**
     * SHA-512 hash with salt.
     *
     * @param strObj input string
     * @param salt   salt value
     * @return hex-encoded SHA-512 digest of (strObj + salt)
     */
    public static String digestSHA512Salt(String strObj, String salt) {
        if (salt == null || salt.isEmpty()) {
            return digestSHA512(strObj);
        }
        String saltedStr = strObj + salt;
        return digestSHA512(saltedStr);
    }

    /**
     * Generates a cryptographically secure random salt.
     *
     * @return hex-encoded 256-bit random salt
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 32 bytes = 256 bits
        random.nextBytes(bytes);
        return byteToString(bytes);
    }
}