package xyz.erupt.core.context;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2021/12/26 23:57
 */
@Getter
@Setter
public class MetaContext {

    private static final ThreadLocal<MetaContext> threadLocal = ThreadLocal.withInitial(() -> {
        MetaContext metaContext = new MetaContext();
        metaContext.setMetaErupt(new MetaErupt());
        metaContext.setMetaUser(new MetaUser());
        return metaContext;
    });

    private MetaErupt metaErupt;

    private MetaUser metaUser;

    private String token;

    private static MetaContext getContext() {
        return threadLocal.get();
    }

    public static MetaErupt getErupt() {
        return getContext().metaErupt;
    }

    public static MetaUser getUser() {
        return getContext().metaUser;
    }

    public static String getToken() {
        return getContext().token;
    }

    //注册erupt上下文
    public static void register(MetaErupt metaErupt) {
        getContext().setMetaErupt(metaErupt);
    }

    //注册用户上下文
    public static void register(MetaUser metaUser) {
        getContext().setMetaUser(metaUser);
    }

    public static void registerToken(String token) {
        getContext().setToken(token);
    }

    public static void remove() {
        threadLocal.remove();
    }

}
