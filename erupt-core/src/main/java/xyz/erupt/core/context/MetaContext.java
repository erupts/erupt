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

    private static final ThreadLocal<MetaContext> threadLocal = InheritableThreadLocal.withInitial(() -> {
        MetaContext metaContext = new MetaContext();
        metaContext.setMetaErupt(new MetaErupt());
        metaContext.setMetaUser(new MetaUser());
        return metaContext;
    });

    public static void set(MetaContext metaContext) {
        threadLocal.set(metaContext);
    }

    public static MetaContext get() {
        return threadLocal.get();
    }

    private MetaErupt metaErupt;

    private MetaUser metaUser;

    private String token;

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

    public static Map<String, Object> getVars() {
        return threadLocal.get().vars;
    }

    //注册erupt上下文
    public static void register(MetaErupt metaErupt) {
        threadLocal.get().setMetaErupt(metaErupt);
    }

    //注册用户上下文
    public static void register(MetaUser metaUser) {
        threadLocal.get().setMetaUser(metaUser);
    }

    public static void registerToken(String token) {
        threadLocal.get().setToken(token);
    }

    public static void registerVar(String key, Object value) {
        threadLocal.get().vars.put(key, value);
    }

    public static void remove() {
        threadLocal.remove();
    }

}
