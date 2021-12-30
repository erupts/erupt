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

    public static MetaContext get() {
        return Optional.ofNullable(threadLocal.get()).orElseGet(() -> {
            MetaContext metaContext = new MetaContext();
            metaContext.setMetaUser(new MetaUser());
            metaContext.setMetaErupt(MetaErupt.builder().build());
            return metaContext;
        });
    }

    //注册erupt上下文
    public static void register(MetaErupt metaErupt) {
        MetaContext metaContext = Optional.ofNullable(threadLocal.get()).orElse(new MetaContext());
        metaContext.setMetaErupt(metaErupt);
        threadLocal.set(metaContext);
    }

    //注册用户上下文
    public static void register(MetaUser metaUser) {
        MetaContext metaContext = Optional.ofNullable(threadLocal.get()).orElse(new MetaContext());
        metaContext.setMetaUser(metaUser);
        threadLocal.set(metaContext);
    }

    public static void remove() {
        threadLocal.remove();
    }

}
