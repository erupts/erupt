package xyz.erupt.upms.service;

import com.google.gson.Gson;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import xyz.erupt.core.config.EruptProp;
import xyz.erupt.core.config.GsonFactory;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private HttpServletRequest request;

    private final Gson gson = GsonFactory.getGson();

    public void put(String key, String str, long timeout) {
        this.put(key, str, timeout, TimeUnit.MINUTES);
    }

    public void put(String key, String str, long timeout, TimeUnit timeUnit) {
        if (eruptProp.isRedisSession()) {
            stringRedisTemplate.opsForValue().set(key, str, timeout, timeUnit);
        } else {
            request.getSession().setAttribute(key, str);
        }
    }

    public void remove(String key) {
        if (eruptProp.isRedisSession()) {
            stringRedisTemplate.delete(key);
        } else {
            request.getSession().removeAttribute(key);
        }
    }

    public Object get(String key) {
        if (eruptProp.isRedisSession()) {
            return stringRedisTemplate.opsForValue().get(key);
        } else {
            return request.getSession().getAttribute(key);
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
            return gson.fromJson(request.getSession().getAttribute(key).toString(), type);
        }
    }

    public void putMap(String key, Map<String, Object> map, long expire) {
        if (eruptProp.isRedisSession()) {
            BoundHashOperations<?, String, Object> boundHashOperations = stringRedisTemplate.boundHashOps(key);
            map.replaceAll((k, v) -> gson.toJson(v));
            boundHashOperations.putAll(map);
            boundHashOperations.expire(expire, TimeUnit.MINUTES);
        } else {
            request.getSession().setAttribute(key, map);
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
            Map<String, T> map = (Map<String, T>) request.getSession().getAttribute(key);
            if (null == map) {
                return null;
            }
            return map.get(mapKey);
        }
    }

}
