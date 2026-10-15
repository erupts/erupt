package xyz.erupt.cloud.node.lite;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Conversions between JSON, JDBC and model member values, and erupt's wire formats. */
final class Values {

    // DATE_TIME on the wire: naive wall-clock time with milliseconds, never shifted by a time zone
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    // "yyyy-MM-dd HH:mm", optionally with seconds and a fraction of 1 to 9 digits
    private static final DateTimeFormatter ACCEPTED = new DateTimeFormatterBuilder().appendPattern("uuuu-MM-dd HH:mm")
            .optionalStart().appendPattern(":ss").optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .toFormatter();

    private static final Map<Class<?>, Class<?>> BOXES = Map.of(boolean.class, Boolean.class, int.class, Integer.class,
            long.class, Long.class, short.class, Short.class, byte.class, Byte.class, double.class, Double.class,
            float.class, Float.class, char.class, Character.class);

    /** What erupt reports as fieldReturnName "number" (TypeUtil.NUMBER_TYPE). */
    static final Set<String> NUMBER_TYPES = Set.of("short", "int", "integer", "long", "float", "double", "bigdecimal", "biginteger");

    private Values() {
    }

    static boolean isNumber(Class<?> type) {
        return NUMBER_TYPES.contains(type.getSimpleName().toLowerCase(Locale.ROOT));
    }

    static boolean isDate(Class<?> type) {
        return type == Date.class || type == LocalDate.class || type == LocalTime.class || type == LocalDateTime.class;
    }

    /** Integral numbers to {@code Long}, others to {@code Double}, so equal values compare equal whatever their type. */
    static Object normalize(Object value) {
        if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
            return ((Number) value).longValue();
        }
        if (value instanceof BigInteger b) return b.bitLength() < 64 ? (Object) b.longValue() : b.doubleValue();
        if (value instanceof BigDecimal d) {
            try {
                return d.longValueExact();
            } catch (ArithmeticException e) {
                return d.doubleValue();
            }
        }
        if (value instanceof Number n) {
            double d = n.doubleValue();
            return d == Math.rint(d) && Math.abs(d) < 9.2e18 ? (Object) (long) d : d;
        }
        return value;
    }

    /** A value as text: dates in erupt's wire formats, enums by name. */
    static String text(Object value) {
        Object json = toJson(value, false);
        return String.valueOf(json);
    }

    /** A member value as JSON: dates in erupt's formats ({@code dateOnly} for a DATE field), enums by name. */
    static Object toJson(Object value, boolean dateOnly) {
        if (value instanceof Date d) value = local(d);
        if (value instanceof LocalDateTime t) return dateOnly ? t.toLocalDate().toString() : t.format(DATE_TIME);
        if (value instanceof LocalDate || value instanceof LocalTime) return value.toString();
        if (value instanceof Enum<?> e) return e.name();
        if (value instanceof Character c) return c.toString();
        return normalize(value);
    }

    /** A value bound into SQL: what JDBC 4.2 drivers take. */
    static Object toDb(Object value) {
        if (value instanceof Date d && !(value instanceof java.sql.Date) && !(value instanceof java.sql.Timestamp)) {
            return local(d);
        }
        if (value instanceof Enum<?> e) return e.name();
        if (value instanceof Character c) return c.toString();
        return value;
    }

    /**
     * {@code value} (from JSON, a driver or a form) as {@code type}; null and blank are null. Throws
     * {@link IllegalArgumentException} with a message fit for the user when it does not convert.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static Object convert(Object value, Class<?> type) {
        if (value instanceof java.sql.Clob clob) value = clob(clob);
        if (value == null || "".equals(value)) return null;
        Class<?> t = BOXES.getOrDefault(type, type);
        if (t.isInstance(value) && t != Date.class) return value;
        if (t == String.class) return text(value);
        if (t == Boolean.class) {
            if (value instanceof Number n) return n.doubleValue() != 0;
            return Set.of("true", "1").contains(text(value).strip().toLowerCase(Locale.ROOT));
        }
        if (Number.class.isAssignableFrom(t)) return number(value, t);
        if (t.isEnum()) {
            try {
                return Enum.valueOf((Class<Enum>) t, text(value));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("illegal option '" + text(value) + "'");
            }
        }
        if (t == Character.class) {
            String s = text(value);
            if (s.length() != 1) throw new IllegalArgumentException("illegal character '" + s + "'");
            return s.charAt(0);
        }
        if (t == LocalTime.class) {
            if (value instanceof java.sql.Time time) return time.toLocalTime();
            try {
                return LocalTime.parse(text(value));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("illegal time '" + text(value) + "'");
            }
        }
        LocalDateTime dt = dateTime(value);
        if (t == LocalDateTime.class) return dt;
        if (t == LocalDate.class) return dt.toLocalDate();
        if (t == Date.class) return Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());
        throw new IllegalArgumentException("unsupported type " + t.getSimpleName());
    }

    private static String clob(java.sql.Clob clob) {
        try {
            return clob.getSubString(1, (int) clob.length());
        } catch (java.sql.SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object number(Object value, Class<?> t) {
        if (value instanceof Boolean b) value = b ? 1 : 0;
        BigDecimal d;
        try {
            d = value instanceof Number n && !(n instanceof BigDecimal) ? new BigDecimal(n.toString()) : new BigDecimal(text(value).strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("illegal number '" + text(value) + "'");
        }
        try {
            if (t == Long.class) return d.longValueExact();
            if (t == Integer.class) return d.intValueExact();
            if (t == Short.class) return d.shortValueExact();
            if (t == Byte.class) return d.byteValueExact();
            if (t == BigInteger.class) return d.toBigIntegerExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("illegal integer '" + text(value) + "'");
        }
        if (t == Double.class) return d.doubleValue();
        if (t == Float.class) return d.floatValue();
        return d;
    }

    private static LocalDateTime dateTime(Object value) {
        if (value instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        if (value instanceof java.sql.Date d) return d.toLocalDate().atStartOfDay();
        if (value instanceof Date d) return local(d);
        if (value instanceof LocalDateTime t) return t;
        if (value instanceof LocalDate d) return d.atStartOfDay();
        if (value instanceof OffsetDateTime o) return o.toLocalDateTime();
        String text = text(value).replace('T', ' ');
        if (text.endsWith("Z")) text = text.substring(0, text.length() - 1);
        try {
            return text.length() == 10 ? LocalDate.parse(text).atStartOfDay() : LocalDateTime.parse(text, ACCEPTED);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("illegal date '" + value + "'");
        }
    }

    private static LocalDateTime local(Date d) {
        if (d instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        if (d instanceof java.sql.Date sd) return sd.toLocalDate().atStartOfDay();
        return LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
    }

    static boolean same(Object a, Object b) {
        return a == null || b == null ? a == b : compare(a, b) == 0;
    }

    /** Orders two non-null values of one kind; numbers compare numerically. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static int compare(Object a, Object b) {
        a = normalize(a);
        b = normalize(b);
        if (a instanceof Number x && b instanceof Number y) return Double.compare(x.doubleValue(), y.doubleValue());
        if (a instanceof Comparable c && a.getClass() == b.getClass()) return c.compareTo(b);
        if (Objects.equals(a, b)) return 0;
        throw new IllegalArgumentException("cannot compare " + a.getClass().getSimpleName() + " with " + b.getClass().getSimpleName());
    }
}
