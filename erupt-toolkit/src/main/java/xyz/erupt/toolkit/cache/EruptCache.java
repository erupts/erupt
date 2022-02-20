package xyz.erupt.toolkit.cache;

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

    public static <V> EruptCache<V> newInstance() {
        return new EruptStandaloneCache<>();
    }

    public V getAndSet(String key, long timeout, Supplier<V> supplier) {
        return Optional.ofNullable(this.get(key)).orElseGet(() -> this.put(key, timeout, supplier.get()));
    }

}
