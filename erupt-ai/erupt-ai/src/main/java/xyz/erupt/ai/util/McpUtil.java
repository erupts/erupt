package xyz.erupt.ai.util;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Date;

/**
 * @author YuePeng
 * date 2025/7/24 00:13
 */

public final class McpUtil {

    // JSON schema type of a tool parameter; a wrong one makes clients send a value Gson cannot bind
    public static String toMcp(Class<?> c) {
        if (c == boolean.class || c == Boolean.class) return "boolean";
        if (c == float.class || c == double.class || c == Float.class || c == Double.class) return "number";
        if (c.isPrimitive() && c != char.class || Number.class.isAssignableFrom(c)) return "integer";
        if (c.isArray() || Collection.class.isAssignableFrom(c)) return "array";
        if (CharSequence.class.isAssignableFrom(c) || c == char.class || c == Character.class || c.isEnum()
                || Date.class.isAssignableFrom(c) || Temporal.class.isAssignableFrom(c)) return "string";
        return "object";
    }
}
