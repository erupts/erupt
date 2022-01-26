package xyz.erupt.toolkit.cache.impl;

import com.google.gson.reflect.TypeToken;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.toolkit.cache.EruptCache;

import javax.annotation.Resource;

/**
 * @author YuePeng
 * date 2022/1/26 22:41
 */
@Component
public class EruptRedisCache<V> extends EruptCache<V> {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    protected V put(String key, long timeout, V v) {
        stringRedisTemplate.opsForValue().set(key, GsonFactory.getGson().toJson(v));
        return v;
    }

    @Override
    protected V get(String key) {
        return GsonFactory.getGson().fromJson(stringRedisTemplate.opsForValue().get(key), new TypeToken<V>() {
        }.getType());
    }
}
