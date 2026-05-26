package xyz.erupt.core.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
     * SHA512加密方法
     *
     * @param strObj 待加密字符串
     * @return 加密后的字符串
     */
    public static String digestSHA512(String strObj) {
        return digestSHA512(strObj, StandardCharsets.UTF_8.name());
    }

    /**
     * SHA512加密方法
     *
     * @param strObj  待加密字符串
     * @param charset 字符编码
     * @return 加密后的字符串
     */
    public static String digestSHA512(String strObj, String charset) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            return byteToString(md.digest(strObj.getBytes(charset)));
        } catch (Exception ex) {
            return strObj;
        }
    }

    /**
     * SHA512加盐加密方法
     *
     * @param strObj 待加密字符串
     * @param salt   盐值
     * @return 加密后的字符串
     */
    public static String digestSHA512Salt(String strObj, String salt) {
        if (salt == null || salt.isEmpty()) {
            return digestSHA512(strObj);
        }
        String saltedStr = strObj + salt;
        return digestSHA512(saltedStr);
    }

    /**
     * 生成随机盐值
     *
     * @return 随机盐值
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 32字节 = 256位
        random.nextBytes(bytes);
        return byteToString(bytes);
    }
}