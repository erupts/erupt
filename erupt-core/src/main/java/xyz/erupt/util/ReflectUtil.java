package xyz.erupt.util;

import java.lang.reflect.Field;

/**
 * Created by liyuepeng on 11/7/18.
 */
public class ReflectUtil {

    //递归查找类字段
    public static Field findClassAllField(Class clazz, String fieldName) {
        Field field = null;
        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName);
                if (null != field) {
                    break;
                }
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return field;
    }
}
