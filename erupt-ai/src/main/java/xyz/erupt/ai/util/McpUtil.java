package xyz.erupt.ai.util;

/**
 * @author YuePeng
 * date 2025/7/24 00:13
 */

public final class McpUtil {

    public static String toMcp(Class<?> c) {
        if (c.isPrimitive()) {
            if (c == boolean.class) return "boolean";
            if (c == float.class || c == double.class) return "number";
            return "integer";   // byte/short/int/long
        }
        if (Number.class.isAssignableFrom(c)) {
            if (c == Float.class || c == Double.class) return "number";
            return "integer";   // Byte/Short/Integer/Long
        }
        return "string";
    }
}
