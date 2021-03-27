package xyz.erupt.upms.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author YuePeng
 * date 2021/3/23 11:31
 */
public class CaffeineEruptCache<V> implements IEruptCache<V> {

    private Cache<String, V> cache;

    public void init(long timeout, TimeUnit timeUnit) {
        if (null == this.cache) {
            synchronized (this) {
                if (null == this.cache) {
                    this.cache = Caffeine.newBuilder().expireAfterWrite(timeout, timeUnit).build();
                }
            }
        }
    }

    public void init(long timeout) {
        this.init(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public V get(String key, Function<String, V> function) {
        return cache.get(key, function);
    }
}
