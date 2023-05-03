package xyz.erupt.core.util;

import lombok.SneakyThrows;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author YuePeng
 * date 2019-01-17.
 */
public class DateUtil {

    public static final String DATE = "yyyy-MM-dd";

    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    public static String getSimpleFormatDateTime(Date date) {
        return getFormatDate(date, DATE_TIME);
    }

    public static String getSimpleFormatDate(Date date) {
        return getFormatDate(date, DATE);
    }

    public static String getFormatDate(Date date, String formatStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
        return sdf.format(date);
    }

    @SneakyThrows
    public static Object getDate(Class<?> targetDateType, String str) {
        if (targetDateType == Date.class) {
            if (str.length() == 10) {
                return new SimpleDateFormat(DATE).parse(str);
            } else {
                return new SimpleDateFormat(DATE_TIME).parse(str);
            }
        } else if (targetDateType == LocalDate.class) {
            return LocalDate.parse(str, DateTimeFormatter.ofPattern(DATE));
        } else if (targetDateType == LocalDateTime.class) {
            return LocalDateTime.parse(str, DateTimeFormatter.ofPattern(DATE_TIME));
        } else {
            throw new EruptWebApiRuntimeException("Unsupported date type");
        }
    }

}
