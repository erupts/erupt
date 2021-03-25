package xyz.erupt.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author YuePeng
 * date 2019-01-17.
 */
public class DateUtil {

    public static final String DATE = "yyyy-MM-dd";

    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    public static String getSimpleFormatDate(Date date) {
        return getFormatDate(date, DATE_TIME);
    }

    public static String getFormatDate(Date date, String formatStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
        return sdf.format(date);
    }

    public static Date getDate(String str) {
        SimpleDateFormat sdf;
        if (str.length() == 10) {
            sdf = new SimpleDateFormat(DATE);
        } else {
            sdf = new SimpleDateFormat(DATE_TIME);
        }
        try {
            return sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
