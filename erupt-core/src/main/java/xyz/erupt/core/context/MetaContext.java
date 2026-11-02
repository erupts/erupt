package xyz.erupt.core.context;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YuePeng
 * date 2021/12/26 23:57
 */
@Getter
@Setter
public class MetaContext {

    // Inheritable so work spawned from a request (async SSE generation, tool execution) starts with
    // the request's identity. A child thread gets its own copy: the JDK default hands the parent's
    // instance over as-is, and a pool thread created from a request thread would then keep sharing
    // one mutable context with it for as long as it lives - every register() of one thread
    // (an erupt name, a user, a tenant) silently becoming the other's
    private static final ThreadLocal<MetaContext> threadLocal = new InheritableThreadLocal<>() {
        @Override
        protected MetaContext initialValue() {
            MetaContext metaContext = new MetaContext();
            metaContext.setMetaErupt(new MetaErupt());
            metaContext.setMetaUser(new MetaUser());
            return metaContext;
        }

        @Override
        protected MetaContext childValue(MetaContext parent) {
            return null == parent ? null : parent.copy();
        }
    };

    private MetaContext copy() {
        MetaContext metaContext = new MetaContext();
        metaContext.metaErupt = null == metaErupt ? null : new MetaErupt(metaErupt.getName(), metaErupt.getMenuValue());
        metaContext.metaUser = null == metaUser ? null : new MetaUser(metaUser.getUid(), metaUser.getAccount(), metaUser.getName(), metaUser.getTenantId());
        metaContext.token = token;
        metaContext.lang = lang;
        metaContext.vars = new HashMap<>(vars);
        return metaContext;
    }

    public static void set(MetaContext metaContext) {
        threadLocal.set(metaContext);
    }

    public static MetaContext get() {
        return threadLocal.get();
    }

    private MetaErupt metaErupt;

    private MetaUser metaUser;

    private String token;

    // Language the console is running in for this request (e.g. zh-CN), resolved from the
    // request and carried here so threads detached from the request - async SSE generation,
    // tool execution - can still answer in the language the user picked
    private String lang;

    private Map<String, Object> vars = new HashMap<>();

    public static MetaErupt getErupt() {
        return threadLocal.get().metaErupt;
    }

    public static MetaUser getUser() {
        return threadLocal.get().metaUser;
    }

    public static String getToken() {
        return threadLocal.get().token;
    }

    public static String getLang() {
        return threadLocal.get().lang;
    }

    public static Map<String, Object> getVars() {
        return threadLocal.get().vars;
    }

    // Register the erupt context
    public static void register(MetaErupt metaErupt) {
        threadLocal.get().setMetaErupt(metaErupt);
    }

    // Register the user context
    public static void register(MetaUser metaUser) {
        threadLocal.get().setMetaUser(metaUser);
    }

    public static void registerToken(String token) {
        threadLocal.get().setToken(token);
    }

    public static void registerLang(String lang) {
        threadLocal.get().setLang(lang);
    }

    public static void registerVar(String key, Object value) {
        threadLocal.get().vars.put(key, value);
    }

    public static void remove() {
        threadLocal.remove();
    }

}
