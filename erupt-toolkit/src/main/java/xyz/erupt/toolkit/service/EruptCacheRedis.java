package xyz.erupt.toolkit.service;

import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import xyz.erupt.core.cache.EruptCache;
import xyz.erupt.core.config.GsonFactory;

import java.util.concurrent.TimeUnit;

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
        stringRedisTemplate.opsForValue().set(key, GsonFactory.getGson().toJson(v), ttl);
        return v;
    }

    @Override
    public V get(String key) {
        return GsonFactory.getGson().fromJson(stringRedisTemplate.opsForValue().get(key), new TypeToken<V>() {
        }.getType());
    }

    @Override
    public void expire(String key, long ttl) {
        stringRedisTemplate.expire(key, ttl, TimeUnit.MILLISECONDS);
    }

    @Override
    public Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key);
    }

    @Override
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

}
