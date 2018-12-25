package xyz.erupt.eruptcache.redis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Created by liyuepeng on 2018-12-24.
 */
@Service
public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    private Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    public void put(String key, String str) {
        redisTemplate.opsForValue().set(key, str);
    }

    public void put(String key, Object obj) {
        redisTemplate.opsForValue().set(key, GSON.toJson(obj));
    }

    public void put(String key, Object obj, int timeout) {
        put(key, obj, timeout, TimeUnit.MINUTES);
    }

    public void put(String key, Object obj, int timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, GSON.toJson(obj), timeout, unit);
    }
}
