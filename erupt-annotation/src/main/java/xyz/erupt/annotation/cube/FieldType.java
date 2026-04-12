package xyz.erupt.annotation.cube;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author YuePeng
 * date 2025/11/4 22:35
 */
public enum FieldType {

    AUTO,
    NUMBER,
    STRING,
    DATE;

    public static FieldType get(FieldType fieldType, Class<?> clazz) {
        if (FieldType.AUTO == fieldType) {
            if (clazz == Short.class || clazz == Integer.class || clazz == Long.class || clazz == Double.class || clazz == Float.class) {
                return FieldType.NUMBER;
            } else if (clazz == Date.class || clazz == java.sql.Date.class || clazz == LocalDateTime.class || clazz == LocalDate.class) {
                return FieldType.DATE;
            } else {
                return FieldType.STRING;
            }
        } else {
            return fieldType;
        }
    }

}
