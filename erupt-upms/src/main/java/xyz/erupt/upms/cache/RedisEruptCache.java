package xyz.erupt.upms.cache;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.data.redis.core.StringRedisTemplate;
import xyz.erupt.core.config.GsonFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author YuePeng
 * date 2021/3/23 11:51
 */
public class RedisEruptCache<V> extends AbstractEruptCache<V> {

    private final StringRedisTemplate redisTemplate;

    private final Gson gson = GsonFactory.getGson();

    public RedisEruptCache(long timeout, TimeUnit timeUnit, StringRedisTemplate redisTemplate) {
        super(timeout, timeUnit);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public V get(String key, Function<String, V> function) {
        //TODO 此方式数据类型转换会出错
        V v = gson.fromJson(redisTemplate.opsForValue().get(key), new TypeToken<V>() {
        }.getType());
        if (null == v) {
            v = function.apply(key);
            redisTemplate.opsForValue().set(key, gson.toJson(v), super.timeout, super.timeUnit);
        }
        return v;
    }

}
