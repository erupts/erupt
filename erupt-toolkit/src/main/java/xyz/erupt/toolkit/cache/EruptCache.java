package xyz.erupt.toolkit.cache;

import org.springframework.data.redis.core.StringRedisTemplate;
import xyz.erupt.core.prop.EruptProp;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.toolkit.cache.impl.EruptRedisCache;
import xyz.erupt.toolkit.cache.impl.EruptStandaloneCache;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author YuePeng
 * date 2021/3/23 14:09
 */
public abstract class EruptCache<V> {

    protected abstract V put(String key, long timeout, V v);

    protected abstract V get(String key);

    public V getAndSet(String key, long timeout, Supplier<V> supplier) {
        return Optional.ofNullable(this.get(key)).orElse(this.put(key, timeout, supplier.get()));
    }

    public static <V> EruptCache<V> factory() {
        if (EruptSpringUtil.getBean(EruptProp.class).isStandaloneCache()) {
            return new EruptStandaloneCache<>();
        } else {
            return new EruptRedisCache<>(EruptSpringUtil.getBean(StringRedisTemplate.class));
        }
    }

}
