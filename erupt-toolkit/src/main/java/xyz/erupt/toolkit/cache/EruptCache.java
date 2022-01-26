package xyz.erupt.toolkit.cache;

import xyz.erupt.toolkit.cache.impl.EruptStandaloneCache;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author YuePeng
 * date 2021/3/23 14:09
 */
public abstract class EruptCache<V> {

    public static <V> EruptCache<V> factory() {
        return new EruptStandaloneCache<>();
    }

    protected abstract V put(String key, long timeout, V v);

    protected abstract V get(String key);

    public V getAndSet(String key, long timeout, Function<String, V> function) {
        return Optional.ofNullable(this.get(key)).orElse(this.put(key, timeout, function.apply(key)));
    }

}
