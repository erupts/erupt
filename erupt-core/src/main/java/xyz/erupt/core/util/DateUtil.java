package xyz.erupt.core.util;

import lombok.SneakyThrows;
import org.apache.commons.lang3.time.DateUtils;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author YuePeng
 * date 2019-01-17.
 */
public class DateUtil {

    public static final String DATE = "yyyy-MM-dd";

    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss.SSS";

    public static final String ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    private static final String[] PATTERNS = {
            "yyyy-MM-dd'T'HH:mm:ss", // ISO 8601 specification，T segmentation
            "yyyy-MM-dd' 'HH:mm:ss.SSS", // SQL/DB TIMESTAMP
            "yyyy-MM-dd' 'HH:mm:ss:SSS", // Non canonical
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "dd-MM-yyyy",
            "yyyy-MM-dd",
            "dd/MM/yyyy"
    };

    /**
     * Compatible with multiple formats of the Date string, parsed into a Date object
     */
    public static Date parseDate(String dateStr) throws ParseException {
        return DateUtils.parseDateStrictly(dateStr, PATTERNS);
    }

    public static String getSimpleFormatDate(Date date) {
        return getFormatDate(date, DATE_TIME);
    }

    public static String getFormatDate(Date date, String formatStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
        return sdf.format(date);
    }

    @SneakyThrows
    public static String dateFormat(Object value, String formatStr) {
        if (value instanceof Date d) {
            return new SimpleDateFormat(formatStr).format(d);
        } else if (value instanceof LocalDate d) {
            return new SimpleDateFormat(formatStr).format(Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        } else if (value instanceof LocalDateTime d) {
            return new SimpleDateFormat(formatStr).format(Date.from(d.atZone(ZoneId.systemDefault()).toInstant()));
        } else {
            return value.toString();
        }
    }

    @SneakyThrows
    public static Object getDate(Class<?> targetDateType, String str) {
        if (targetDateType == Date.class) {
            if (str.length() == 10) {
                return new SimpleDateFormat(DATE).parse(str);
            } else {
                return new SimpleDateFormat(ISO_8601).parse(str);
            }
        } else if (targetDateType == LocalDate.class) {
            return LocalDate.parse(str.substring(0, 10), DateTimeFormatter.ofPattern(DATE));
        } else if (targetDateType == LocalDateTime.class) {
            return LocalDateTime.parse(str, DateTimeFormatter.ofPattern(ISO_8601));
        } else {
            throw new EruptWebApiRuntimeException("Unsupported date type");
        }
    }

}
