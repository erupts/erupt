package xyz.erupt.core.util;

import lombok.SneakyThrows;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author YuePeng
 * date 2023/5/9 21:34
 */
public class SecretUtil {


    public static String decodeSecret(String str) {
        return new String(Base64.getDecoder().decode(str));
    }

    @SneakyThrows
    public static String decodeSecret(String str, int encodeNum) {
        for (int i = 0; i < encodeNum; i++) {
            str = decodeSecret(str);
        }
        return URLDecoder.decode(str, StandardCharsets.UTF_8.name());
    }

}
