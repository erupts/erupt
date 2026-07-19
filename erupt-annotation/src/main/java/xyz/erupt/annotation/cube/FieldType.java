package xyz.erupt.annotation.cube;

import xyz.erupt.annotation.fun.ChoiceFetchHandler;
import xyz.erupt.annotation.fun.VLModel;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author YuePeng
 * date 2025/11/4 22:35
 */
public enum FieldType {

    AUTO,
    NUMBER,
    STRING,
    DATE;

    public static FieldType getType(Field field) {
        Class<?> clazz = field.getType();
        if (clazz == Short.class || clazz == Integer.class || clazz == Long.class || clazz == Double.class || clazz == Float.class) {
            return FieldType.NUMBER;
        } else if (clazz == Date.class || clazz == java.sql.Date.class || clazz == LocalDateTime.class || clazz == LocalDate.class) {
            return FieldType.DATE;
        } else {
            return FieldType.STRING;
        }
    }

    public static class H implements ChoiceFetchHandler<Void> {

        @Override
        public List<VLModel> fetch(String[] params) {
            return Arrays.stream(FieldType.values()).filter(it -> !it.name().equals(AUTO.name()))
                    .map(v -> new VLModel(v.name(), v.name())).toList();
        }

    }

}
