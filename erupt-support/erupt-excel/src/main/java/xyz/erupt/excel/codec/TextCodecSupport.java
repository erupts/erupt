package xyz.erupt.excel.codec;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Cell rendering shared by the text formats.
 */
final class TextCodecSupport {

    private TextCodecSupport() {
    }

    static String text(Object value) {
        if (null == value) return "";
        if (value instanceof LocalDateTime d) return d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (value instanceof LocalDate d) return d.toString();
        return value.toString();
    }

}
