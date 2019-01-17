package xyz.erupt.core.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by liyuepeng on 2019-01-17.
 */
public class DateUtil {

    public static final String DATE = "yyyy-MM-dd";

    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    public static String getFormatDate(String formatStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
        return sdf.format(new Date());
    }
}
