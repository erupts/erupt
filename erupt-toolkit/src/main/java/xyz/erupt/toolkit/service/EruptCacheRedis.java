package xyz.erupt.toolkit.service;

import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import xyz.erupt.core.cache.EruptCache;
import xyz.erupt.core.config.GsonFactory;

/**
 * @author YuePeng
 * date 2022/1/26 22:41
 */
@Component
@AllArgsConstructor
public class EruptCacheRedis<V> implements EruptCache<V> {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public V put(String key, V v, long ttl) {
        stringRedisTemplate.opsForValue().set(key, GsonFactory.getGson().toJson(v));
        return v;
    }

    @Override
    public V get(String key) {
        return GsonFactory.getGson().fromJson(stringRedisTemplate.opsForValue().get(key), new TypeToken<V>() {
        }.getType());
    }
}
