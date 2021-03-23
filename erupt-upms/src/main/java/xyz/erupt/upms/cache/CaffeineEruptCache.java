package xyz.erupt.upms.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author YuePeng
 * date 2021/3/23 11:31
 */
public class CaffeineEruptCache<V> extends AbstractEruptCache<V> {

    Cache<String, V> cache;

    public CaffeineEruptCache(long timeout, TimeUnit timeUnit) {
        super(timeout, timeUnit);
        this.cache = Caffeine.newBuilder().expireAfterWrite(timeout, timeUnit).build();
    }

    @Override
    public V get(String key, Function<String, V> function) {
        return cache.get(key, function);
    }
}
