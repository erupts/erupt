package xyz.erupt.core.proxy;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;

/**
 * 代理类上下文
 *
 * @author YuePeng
 * date 2022/3/12 00:13
 */
@Getter
@Setter
public class ProxyContext {

    private static final ThreadLocal<ProxyContext> proxyContextThreadLocal = ThreadLocal.withInitial(ProxyContext::new);

    private Class<?> clazz;

    private Field field;

    public static void set(Class<?> clazz) {
        proxyContextThreadLocal.get().setClazz(clazz);
    }

    public static void set(Field field) {
        proxyContextThreadLocal.get().setField(field);
    }

    public static void remove() {
        proxyContextThreadLocal.remove();
    }

    public static ProxyContext get() {
        return proxyContextThreadLocal.get();
    }

}
