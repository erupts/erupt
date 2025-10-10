package xyz.erupt.upms.service;

import com.google.gson.Gson;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.core.prop.EruptProp;

import jakarta.annotation.Resource;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author YuePeng
 * date 2019-08-13.
 */
@Component
public class EruptSessionService {

    @Resource
    private EruptProp eruptProp;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private EruptLocalSession eruptLocalSession;

    private final Gson gson = GsonFactory.getGson();

    public void put(String key, String str, long timeout, TimeUnit timeUnit) {
        if (eruptProp.isRedisSession()) {
            stringRedisTemplate.opsForValue().set(key, str, timeout, timeUnit);
        } else {
            eruptLocalSession.put(key, str, timeUnit.toMillis(timeout));
        }
    }

    public Long increment(String key, long timeout, TimeUnit timeUnit) {
        if (eruptProp.isRedisSession()) {
            try {
                return stringRedisTemplate.opsForValue().increment(key, 1);
            } finally {
                stringRedisTemplate.expire(key, timeout, timeUnit);
            }
        } else {
            synchronized (this) {
                Long num = (Long) eruptLocalSession.get(key);
                if (null == num) num = 0L;
                eruptLocalSession.put(key, ++num, timeUnit.toMillis(timeout));
                return num;
            }
        }
    }

    public boolean exist(String key) {
        if (eruptProp.isRedisSession()) {
            return stringRedisTemplate.hasKey(key);
        } else {
            return null != eruptLocalSession.get(key);
        }
    }

    public void remove(String key) {
        if (eruptProp.isRedisSession()) {
            stringRedisTemplate.delete(key);
        } else {
            eruptLocalSession.delete(key);
        }
    }

    public void expire(String key, long timeout, TimeUnit unit) {
        if (eruptProp.isRedisSession()) {
            stringRedisTemplate.expire(key, timeout, unit);
        } else {
            eruptLocalSession.expire(key, unit.toMillis(timeout));
        }
    }

    public Object get(String key) {
        if (eruptProp.isRedisSession()) {
            return stringRedisTemplate.opsForValue().get(key);
        } else {
            return eruptLocalSession.get(key);
        }
    }

    public <T> T get(String key, Type type) {
        if (eruptProp.isRedisSession()) {
            if (null == this.get(key)) {
                return null;
            } else {
                return gson.fromJson(this.get(key).toString(), type);
            }
        } else {
            return gson.fromJson(eruptLocalSession.get(key).toString(), type);
        }
    }

    public void putMap(String key, Map<String, Object> map, long expire, TimeUnit timeUnit) {
        if (eruptProp.isRedisSession()) {
            BoundHashOperations<?, String, Object> boundHashOperations = stringRedisTemplate.boundHashOps(key);
            map.replaceAll((k, v) -> gson.toJson(v));
            boundHashOperations.putAll(map);
            boundHashOperations.expire(expire, timeUnit);
        } else {
            eruptLocalSession.put(key, map, timeUnit.toMillis(expire));
        }
    }

    //获取map的所有key
    public List<String> getMapKeys(String key) {
        if (eruptProp.isRedisSession()) {
            Set<Object> set = stringRedisTemplate.opsForHash().keys(key);
            return set.stream().map(Object::toString).collect(Collectors.toList());
        } else {
            Map<String, Object> map = (Map<String, Object>) eruptLocalSession.get(key);
            if (null == map) {
                return null;
            }
            return new ArrayList<>(map.keySet());
        }
    }

    public <T> T getMapValue(String key, String mapKey, Class<T> type) {
        if (eruptProp.isRedisSession()) {
            Object obj = stringRedisTemplate.boundHashOps(key).get(mapKey);
            if (null == obj) {
                return null;
            }
            return gson.fromJson(obj.toString(), type);
        } else {
            Map<String, T> map = (Map<String, T>) eruptLocalSession.get(key);
            if (null == map) {
                return null;
            }
            return map.get(mapKey);
        }
    }

}
