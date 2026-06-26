package xyz.erupt.designer.proxy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.util.EruptProxyUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;

/**
 * Disguise designer json as a real annotation instance: every member present in the json
 * overrides the template annotation, everything else falls through to the template defaults.
 * <p>
 * Nested annotations are proxied recursively; annotation array members are expanded from the
 * first template element (prototype).
 *
 * @author YuePeng
 * date 2026-06-12
 */
public class JsonAnnotationProxy {

    public static <A extends Annotation> A proxy(A annotation, JsonObject json) {
        if (null == json) return annotation;
        return EruptProxyUtil.newProxy(annotation, invocation -> {
            JsonElement je = json.get(invocation.getMethod().getName());
            if (null == je || je.isJsonNull()) return invocation.proceed();
            return toValue(invocation.getMethod().getReturnType(), je, invocation);
        });
    }

    private static Object toValue(Class<?> rt, JsonElement je, MethodInvocation invocation) throws Throwable {
        if (Class.class.isAssignableFrom(rt)) return invocation.proceed();
        if (rt.isAnnotation()) {
            return proxy((Annotation) invocation.proceed(), je.getAsJsonObject());
        }
        if (rt.isArray()) {
            Class<?> ct = rt.getComponentType();
            JsonArray arr = je.getAsJsonArray();
            Object result = Array.newInstance(ct, arr.size());
            Annotation prototype = null;
            if (ct.isAnnotation() && !arr.isEmpty()) {
                Object[] template = (Object[]) invocation.proceed();
                if (template != null && template.length == 0) {
                    throw new EruptWebApiRuntimeException("No annotation prototype in template: "
                            + invocation.getMethod().getName());
                }
                prototype = (Annotation) template[0];
            }
            for (int i = 0; i < arr.size(); i++) {
                Array.set(result, i, ct.isAnnotation()
                        ? proxy(prototype, arr.get(i).getAsJsonObject()) : scalar(ct, arr.get(i)));
            }
            return result;
        }
        return scalar(rt, je);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object scalar(Class<?> rt, JsonElement je) {
        if (rt.isEnum()) return Enum.valueOf((Class<? extends Enum>) rt, je.getAsString());
        if (rt == String.class) return je.getAsString();
        if (rt == boolean.class || rt == Boolean.class) return je.getAsBoolean();
        if (rt == int.class || rt == Integer.class) return je.getAsInt();
        if (rt == long.class || rt == Long.class) return je.getAsLong();
        if (rt == float.class || rt == Float.class) return je.getAsFloat();
        if (rt == double.class || rt == Double.class) return je.getAsDouble();
        throw new EruptWebApiRuntimeException("Unsupported annotation member type: " + rt.getName());
    }

}
