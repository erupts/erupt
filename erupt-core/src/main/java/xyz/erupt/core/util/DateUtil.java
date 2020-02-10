package xyz.erupt.core.util;

import xyz.erupt.annotation.sub_field.sub_edit.DateType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author liyuepeng
 * @date 2019-01-17.
 */
public class DateUtil {

    public static final String DATE = "yyyy-MM-dd";

    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    public static String getFormatDate(Date date, String formatStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
        return sdf.format(date);
    }

    public static Date getDate(String str, DateType dateType) {
        SimpleDateFormat sdf;
        if (dateType.type() == DateType.Type.DATE) {
            sdf = new SimpleDateFormat(DATE);
        } else if (dateType.type() == DateType.Type.DATE_TIME) {
            sdf = new SimpleDateFormat(DATE_TIME);
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
