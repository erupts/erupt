package xyz.erupt.core.context;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

/**
 * @author YuePeng
 * date 2021/12/26 23:57
 */
@Getter
@Setter
public class MetaContext {

    private static final ThreadLocal<MetaContext> threadLocal = new ThreadLocal<>();

    private MetaErupt metaErupt;

    private MetaUser metaUser;

    private String token;

    private static MetaContext getContext() {
        return Optional.ofNullable(threadLocal.get()).orElseGet(() -> {
            MetaContext metaContext = new MetaContext();
            metaContext.setMetaErupt(new MetaErupt());
            metaContext.setMetaUser(new MetaUser());
            threadLocal.set(metaContext);
            return metaContext;
        });
    }

    public static MetaErupt getErupt() {
        return Optional.ofNullable(getContext().metaErupt).orElse(new MetaErupt());
    }

    public static MetaUser getUser() {
        return Optional.ofNullable(getContext().metaUser).orElse(new MetaUser());
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
