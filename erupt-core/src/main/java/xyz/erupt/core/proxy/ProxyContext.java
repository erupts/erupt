package xyz.erupt.core.proxy;

import java.lang.reflect.Field;

/**
 * 代理类上下文
 * @author YuePeng
 * date 2022/3/12 00:13
 */
public class ProxyContext {

    private static Field field;

    private static Class<?> clazz;

    public static Field getField() {
        return field;
    }

    public static void setField(Field fieldContext) {
        ProxyContext.field = fieldContext;
    }

    public static Class<?> getClazz() {
        return clazz;
    }

    public static void setClazz(Class<?> clazz) {
        ProxyContext.clazz = clazz;
    }
}
