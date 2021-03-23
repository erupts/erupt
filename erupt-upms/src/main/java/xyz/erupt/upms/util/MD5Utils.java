package xyz.erupt.upms.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * @author YuePeng
 * date 2018-10-10.
 */
public class MD5Utils {

    private final static String[] STR_DIGITS = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    private MD5Utils() {
    }

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
}
