package xyz.erupt.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author YuePeng
 * date 2019-07-11.
 */
@Slf4j
public class MimeUtil {

    private static final Properties mimes = new Properties();

    static {
        try (InputStream in = MimeUtil.class.getClassLoader().getResourceAsStream("mime.properties")) {
            mimes.load(in);
        } catch (IOException e) {
            log.warn("mime file load error", e);
        }
    }

    public static String getMimeType(String fileName) {
        String[] nameSplits = fileName.split("\\.");
        String type = mimes.getProperty(nameSplits[nameSplits.length - 1]);
        if (StringUtils.isBlank(type)) {
            type = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return type;
    }

}
