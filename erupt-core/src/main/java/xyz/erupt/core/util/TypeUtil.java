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
        targetType = targetType.toLowerCase();
        Serializable o;
        if ("float".equals(targetType)) {
            o = new Float(serializable.toString());
        } else if ("double".equals(targetType)) {
            o = new Double(serializable.toString());
        } else if ("long".equals(targetType)) {
            o = Long.valueOf(serializable.toString());
        } else if ("int".equals(targetType) || "integer".equals(targetType)) {
            o = Integer.valueOf(serializable.toString());
        } else {
            o = serializable.toString();
        }
        return o;
    }

    public static void simpleNumberTypeArrayToObject(Object obj, String type, Consumer<Number> consumer) {
        if ("int".equals(type)) {
            int[] intArray = (int[]) obj;
            for (Number i : intArray) {
                consumer.accept(i);
            }
        } else if ("short".equals(type)) {
            short[] intArray = (short[]) obj;
            for (Number i : intArray) {
                consumer.accept(i);
            }
        } else if ("long".equals(type)) {
            long[] intArray = (long[]) obj;
            for (Number i : intArray) {
                consumer.accept(i);
            }
        } else if ("float".equals(type)) {
            float[] intArray = (float[]) obj;
            for (Number i : intArray) {
                consumer.accept(i);
            }
        } else if ("double".equals(type)) {
            double[] intArray = (double[]) obj;
            for (Number i : intArray) {
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
