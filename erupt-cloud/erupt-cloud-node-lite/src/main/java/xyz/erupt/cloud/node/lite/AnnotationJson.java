package xyz.erupt.cloud.node.lite;

import xyz.erupt.annotation.config.EruptProperty;
import xyz.erupt.annotation.config.Match;
import xyz.erupt.annotation.config.ToMap;

import java.beans.Transient;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Annotation to wire JSON by the rules of erupt's {@code AnnotationProcess.annotationToJsonByReflect}: one key per
 * attribute ({@code @EruptProperty} alias applied), {@code @Transient} attributes skipped, {@code @Match} attributes
 * only when their condition holds, enums by name, classes by simple name, {@code @ToMap} arrays as objects.
 * Attribute values come through a {@link Resolver}, which plays the part of erupt's annotation proxies.
 */
final class AnnotationJson {

    /** The value of one attribute: the raw one, or what erupt's proxy would answer for it. */
    @FunctionalInterface
    interface Resolver {
        Object value(Annotation annotation, Method attribute);
    }

    // the @Match conditions erupt-annotation declares: "#item.x().toString()=='V'" joined by ||, "#value.x() != ''"
    private static final Pattern TERM = Pattern.compile("#(item|value)\\.(\\w+)\\(\\)(?:\\.toString\\(\\))?\\s*(==|!=)\\s*'([^']*)'");

    private AnnotationJson() {
    }

    static Object raw(Annotation annotation, Method attribute) {
        try {
            return attribute.invoke(annotation);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    static Map<String, Object> toJson(Annotation annotation, Resolver resolver) {
        Map<String, Object> json = new LinkedHashMap<>();
        for (Method m : annotation.annotationType().getDeclaredMethods()) {
            Transient t = m.getAnnotation(Transient.class);
            if (t != null && t.value()) continue;
            EruptProperty property = m.getAnnotation(EruptProperty.class);
            String key = property != null && !property.alias().isEmpty() ? property.alias() : m.getName();
            Object value = resolver.value(annotation, m);
            Match match = m.getAnnotation(Match.class);
            if (match != null && !matches(match.value(), annotation, value, resolver)) continue;
            ToMap toMap = m.getAnnotation(ToMap.class);
            if (toMap != null) {
                Map<String, Object> map = new LinkedHashMap<>();
                for (Object item : (Object[]) value) {
                    Map<String, Object> entry = toJson((Annotation) item, resolver);
                    map.put(String.valueOf(entry.remove(toMap.key())), entry);
                }
                if (!map.isEmpty()) json.put(key, map);
            } else {
                json.put(key, plain(value, resolver));
            }
        }
        return json;
    }

    private static Object plain(Object value, Resolver resolver) {
        if (value instanceof Annotation a) return toJson(a, resolver);
        if (value instanceof Class<?> c) return c.getSimpleName();
        if (value instanceof Enum<?> e) return e.toString();
        if (value instanceof Character || value instanceof Byte) return value.toString();
        if (value != null && value.getClass().isArray()) {
            List<Object> list = new ArrayList<>();
            for (int i = 0; i < Array.getLength(value); i++) list.add(plain(Array.get(value, i), resolver));
            return list;
        }
        return value;
    }

    /** Evaluates the {@code @Match} expressions erupt-annotation uses; an unknown form keeps the attribute. */
    static boolean matches(String expression, Annotation item, Object value, Resolver resolver) {
        for (String term : expression.split("\\|\\|")) {
            Matcher m = TERM.matcher(term.strip());
            if (!m.matches()) return true;
            Annotation target = m.group(1).equals("item") ? item : (Annotation) value;
            Object actual;
            try {
                Method attribute = target.annotationType().getMethod(m.group(2));
                actual = target == item ? resolver.value(item, attribute) : raw(target, attribute);
            } catch (NoSuchMethodException e) {
                return true;
            }
            if (String.valueOf(actual).equals(m.group(4)) == m.group(3).equals("==")) return true;
        }
        return false;
    }
}
