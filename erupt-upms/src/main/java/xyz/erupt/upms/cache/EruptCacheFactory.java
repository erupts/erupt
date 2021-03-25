package xyz.erupt.upms.cache;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import xyz.erupt.core.config.EruptProp;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author YuePeng
 * date 2021/3/23 11:52
 */
@Service
public class EruptCacheFactory {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private EruptProp eruptProp;

    public <V> AbstractEruptCache<V> getInstance(long timeout) {
//        if (eruptProp.isRedisSession()) {
//            return new RedisEruptCache<>(timeout, TimeUnit.MILLISECONDS, stringRedisTemplate);
//        } else {
//            return new CaffeineEruptCache<>(timeout, TimeUnit.MILLISECONDS);
//        }
        return new CaffeineEruptCache<>(timeout, TimeUnit.MILLISECONDS);
    }

}
