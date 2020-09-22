package xyz.erupt.core.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * @author liyuepeng
 * @date 2018-11-01.
 */
public class TypeUtil {
    private static final String[] SIMPLE_JPA_TYPE = {
            "byte", "short", "int", "integer", "long", "float", "double", "boolean", "char", "String", "date", "character", "char"
    };
    private static final String[] NUMBER_TYPE = {
            "short", "int", "integer", "long", "float", "double", "bigdecimal", "biginteger"
    };

    private static Pattern NUMBER_PATTERN = Pattern.compile("^[-+]?\\d+(\\.\\d+)?$");


    /**
     * 将未知类型转换为目标类型（目标类型只支持基本数据类型）
     *
     * @param serializable
     * @param targetType
     * @return
     */
    public static Serializable typeStrConvertObject(Serializable serializable, String targetType) {
        Serializable o;
        if (int.class.getSimpleName().equals(targetType) || Integer.class.getSimpleName().equals(targetType)) {
            o = Integer.valueOf(serializable.toString());
        } else if (short.class.getSimpleName().equalsIgnoreCase(targetType)) {
            o = Short.valueOf(serializable.toString());
        } else if (long.class.getSimpleName().equalsIgnoreCase(targetType)) {
            o = Long.valueOf(serializable.toString());
        } else if (float.class.getSimpleName().equalsIgnoreCase(targetType)) {
            o = Float.valueOf(serializable.toString());
        } else if (double.class.getSimpleName().equalsIgnoreCase(targetType)) {
            o = Double.valueOf(serializable.toString());
        } else if (boolean.class.getSimpleName().equalsIgnoreCase(targetType)) {
            o = Boolean.valueOf(serializable.toString());
        } else {
            o = serializable.toString();
        }
        return o;
    }

    public static void simpleNumberTypeArrayToObject(Object obj, String type, Consumer<Number> consumer) {
        if (int.class.getSimpleName().equals(type)) {
            int[] array = (int[]) obj;
            for (Number i : array) {
                consumer.accept(i);
            }
        } else if (short.class.getSimpleName().equals(type)) {
            short[] array = (short[]) obj;
            for (Number i : array) {
                consumer.accept(i);
            }
        } else if (long.class.getSimpleName().equals(type)) {
            long[] array = (long[]) obj;
            for (Number i : array) {
                consumer.accept(i);
            }
        } else if (float.class.getSimpleName().equals(type)) {
            float[] array = (float[]) obj;
            for (Number i : array) {
                consumer.accept(i);
            }
        } else if (double.class.getSimpleName().equals(type)) {
            double[] array = (double[]) obj;
            for (Number i : array) {
                consumer.accept(i);
            }
        }
    }

    /**
     * 判断实体类字段返回值是否为基本类型（包括String与date）
     *
     * @return
     */
    public static boolean isFieldSimpleType(String typeName) {
        return Arrays.asList(SIMPLE_JPA_TYPE).contains(typeName.toLowerCase());
    }

    public static boolean isNumberType(String typeName) {
        return Arrays.asList(NUMBER_TYPE).contains(typeName.toLowerCase());
    }


    public static boolean isNumeric(String str) {
        return NUMBER_PATTERN.matcher(str).matches();
    }

}
