package xyz.erupt.cloud.node.lite;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The JSON codec of the SDK. Parses into {@code Map} (insertion ordered), {@code List}, {@code String},
 * {@code Long} / {@code Double}, {@code Boolean} and {@code null}; writes those plus any {@code Number}, arrays,
 * enums and dates (erupt's wire formats).
 */
public final class Json {

    private final String s;
    private int i;

    private Json(String s) {
        this.s = s;
    }

    /** Parses one JSON document; throws {@link IllegalArgumentException} on malformed input. */
    public static Object parse(String text) {
        Json p = new Json(text);
        try {
            Object value = p.value();
            p.skip();
            if (p.i != text.length()) throw p.error();
            return value;
        } catch (IndexOutOfBoundsException e) {
            throw p.error();
        }
    }

    public static String write(Object value) {
        StringBuilder sb = new StringBuilder();
        write(sb, value);
        return sb.toString();
    }

    // ---------------------------------------------------------------- reading

    private Object value() {
        skip();
        char c = s.charAt(i);
        switch (c) {
            case '{' -> {
                Map<String, Object> map = new LinkedHashMap<>();
                i++;
                if (peek() == '}') {
                    i++;
                    return map;
                }
                do {
                    skip();
                    if (s.charAt(i) != '"') throw error();
                    String key = string();
                    if (peek() != ':') throw error();
                    i++;
                    map.put(key, value());
                } while (next(',', '}'));
                return map;
            }
            case '[' -> {
                List<Object> list = new ArrayList<>();
                i++;
                if (peek() == ']') {
                    i++;
                    return list;
                }
                do {
                    list.add(value());
                } while (next(',', ']'));
                return list;
            }
            case '"' -> {
                return string();
            }
            default -> {
                if (s.startsWith("true", i)) return literal(4, true);
                if (s.startsWith("false", i)) return literal(5, false);
                if (s.startsWith("null", i)) return literal(4, null);
                return number();
            }
        }
    }

    private boolean next(char more, char end) {
        char c = peek();
        i++;
        if (c == more) return true;
        if (c == end) return false;
        throw error();
    }

    private Object literal(int length, Object value) {
        i += length;
        return value;
    }

    private Object number() {
        int start = i;
        while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
        String text = s.substring(start, i);
        try {
            if (text.indexOf('.') < 0 && text.indexOf('e') < 0 && text.indexOf('E') < 0) {
                try {
                    return Long.parseLong(text);
                } catch (NumberFormatException e) {
                    return new BigInteger(text).doubleValue();
                }
            }
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw error();
        }
    }

    private String string() {
        StringBuilder sb = new StringBuilder();
        i++;
        for (char c; (c = s.charAt(i++)) != '"'; ) {
            if (c < 0x20) throw error();
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            char e = s.charAt(i++);
            switch (e) {
                case '"', '\\', '/' -> sb.append(e);
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 'n' -> sb.append('\n');
                case 'r' -> sb.append('\r');
                case 't' -> sb.append('\t');
                case 'u' -> {
                    sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                    i += 4;
                }
                default -> throw error();
            }
        }
        return sb.toString();
    }

    private char peek() {
        skip();
        return s.charAt(i);
    }

    private void skip() {
        while (i < s.length() && " \t\r\n".indexOf(s.charAt(i)) >= 0) i++;
    }

    private IllegalArgumentException error() {
        return new IllegalArgumentException("malformed JSON at " + i);
    }

    // ---------------------------------------------------------------- writing

    private static void write(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String str) {
            quote(sb, str);
        } else if (value instanceof Boolean || value instanceof Long || value instanceof Integer || value instanceof Short
                   || value instanceof Byte || value instanceof BigInteger) {
            sb.append(value);
        } else if (value instanceof BigDecimal d) {
            sb.append(d.toPlainString());
        } else if (value instanceof Number n) {
            double d = n.doubleValue();
            sb.append(Double.isFinite(d) ? String.valueOf(d) : "null");
        } else if (value instanceof Map<?, ?> map) {
            sb.append('{');
            boolean first = true;
            for (var e : map.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                quote(sb, String.valueOf(e.getKey()));
                sb.append(':');
                write(sb, e.getValue());
            }
            sb.append('}');
        } else if (value instanceof Collection<?> list) {
            sb.append('[');
            boolean first = true;
            for (Object v : list) {
                if (!first) sb.append(',');
                first = false;
                write(sb, v);
            }
            sb.append(']');
        } else if (value instanceof Object[] array) {
            write(sb, Arrays.asList(array));
        } else if (value instanceof Record record) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (RecordComponent c : record.getClass().getRecordComponents()) {
                try {
                    Method accessor = c.getAccessor();
                    accessor.setAccessible(true);
                    map.put(c.getName(), accessor.invoke(record));
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(e);
                }
            }
            write(sb, map);
        } else {
            Object json = Values.toJson(value, false);
            if (json == value) quote(sb, value.toString());
            else write(sb, json);
        }
    }

    private static void quote(StringBuilder sb, String str) {
        sb.append('"');
        for (int k = 0; k < str.length(); k++) {
            char c = str.charAt(k);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20 || c == ' ' || c == ' ') sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
    }
}
