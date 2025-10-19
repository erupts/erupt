package xyz.erupt.core.util;

import lombok.SneakyThrows;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author YuePeng
 * date 2018-11-01.
 */
public class TypeUtil {
    private static final String[] SIMPLE_JPA_TYPE = {
            "byte", "short", "int", "integer", "long", "float", "double", "boolean", "char", "String", "date", "character", "char"
    };
    private static final String[] NUMBER_TYPE = {
            "short", "int", "integer", "long", "float", "double", "bigdecimal", "biginteger"
    };

    /**
     * Convert the unknown type to the target type
     */
    @SneakyThrows
    public static Object typeStrConvertObject(Object obj, Class<?> targetType) {
        String val = obj.toString();
        String str = val;
        if (NumberUtils.isCreatable(val)) {
            if (val.endsWith(".0")) {
                val = val.substring(0, val.length() - 2);
            }
        }
        if (int.class == targetType || Integer.class == targetType) {
            return Integer.valueOf(val);
        } else if (short.class == targetType || Short.class == targetType) {
            return Short.valueOf(val);
        } else if (long.class == targetType || Long.class == targetType) {
            return Long.valueOf(val);
        } else if (float.class == targetType || Float.class == targetType) {
            return Float.valueOf(val);
        } else if (double.class == targetType || Double.class == targetType) {
            return Double.valueOf(val);
        } else if (BigDecimal.class == targetType) {
            return new BigDecimal(val);
        } else if (boolean.class == targetType || Boolean.class == targetType) {
            return Boolean.valueOf(val);
        } else if (targetType.isEnum()) {
            return targetType.getMethod("valueOf", String.class).invoke(targetType, val);
        } else {
            return str;
        }
    }

    public static void simpleNumberTypeArrayToObject(Object obj, String type, Consumer<Number> consumer) {
        if (int.class.getSimpleName().equals(type)) {
            for (Number i : (int[]) obj) {
                consumer.accept(i);
            }
        } else if (short.class.getSimpleName().equals(type)) {
            for (Number i : (short[]) obj) {
                consumer.accept(i);
            }
        } else if (long.class.getSimpleName().equals(type)) {
            for (Number i : (long[]) obj) {
                consumer.accept(i);
            }
        } else if (float.class.getSimpleName().equals(type)) {
            for (Number i : (float[]) obj) {
                consumer.accept(i);
            }
        } else if (double.class.getSimpleName().equals(type)) {
            for (Number i : (double[]) obj) {
                consumer.accept(i);
            }
        }
    }

    // Determine whether the return value of the entity class field is a basic type (including String and date)
    public static boolean isFieldSimpleType(String typeName) {
        return Arrays.asList(SIMPLE_JPA_TYPE).contains(typeName.toLowerCase());
    }

    public static boolean isNumberType(String typeName) {
        return Arrays.asList(NUMBER_TYPE).contains(typeName.toLowerCase());
    }

    // Determine whether it is a numeric type
    public static boolean isNumber(Object obj) {
        return obj instanceof Number || NumberUtils.isCreatable(obj.toString());
    }

    public static Integer fetchInt(Object value) {
        if (value instanceof String) {
            return Integer.parseInt(value.toString());
        } else if (value instanceof Integer) {
            return (Integer) value;
        } else {
            return ((Double) value).intValue();
        }
    }

    public static String arrayToConditonString(List<Object> objects, Class<?> target) {
        List<String> values = new ArrayList<>();
        for (Object o : objects) {
            Object val = TypeUtil.typeStrConvertObject(o, target);
            if (val instanceof String) val = "'" + val + "'";
            values.add(val.toString());
        }
        return String.join(",", values);
    }

}
