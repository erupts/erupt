package xyz.erupt.core.proxy;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.core.i18n.I18nTranslate;
import xyz.erupt.core.util.EruptSpringUtil;

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

    private boolean i18n = false;

    public static void set(Class<?> clazz) {
        proxyContextThreadLocal.get().setClazz(clazz);
        proxyContextThreadLocal.get().setI18n(null != clazz.getAnnotation(EruptI18n.class));
    }

    public static void set(Field field) {
        proxyContextThreadLocal.get().setField(field);
        ProxyContext.set(field.getDeclaringClass());
    }

    public static ProxyContext get() {
        return proxyContextThreadLocal.get();
    }

    public static void remove() {
        proxyContextThreadLocal.remove();
    }

    public static String translate(String key, Object... args) {
        if (ProxyContext.get().i18n) {
            return EruptSpringUtil.getBean(I18nTranslate.class).translate(key, args);
        } else {
            return String.format(key, args);
        }
    }

}
