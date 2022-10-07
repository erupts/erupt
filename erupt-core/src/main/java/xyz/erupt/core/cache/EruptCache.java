package xyz.erupt.core.cache;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author YuePeng
 * date 2021/3/23 14:09
 */
public interface EruptCache<V> {

    V put(String key, V v, long ttl);

    V get(String key);

    default V getAndSet(String key, long timeout, Supplier<V> supplier) {
        return Optional.ofNullable(this.get(key)).orElseGet(() -> this.put(key, supplier.get(), timeout));
    }

}
