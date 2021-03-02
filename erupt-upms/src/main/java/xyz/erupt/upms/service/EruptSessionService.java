package xyz.erupt.upms.service;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import xyz.erupt.core.config.EruptProp;
import xyz.erupt.core.config.GsonFactory;
import xyz.erupt.upms.config.EruptUpmsConfig;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

/**
 * @author liyuepeng
 * @date 2019-08-13.
 */
@Component
public class EruptSessionService {

    @Autowired
    private EruptProp eruptProp;

    @Autowired
    private EruptUpmsConfig eruptUpmsConfig;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private HttpServletRequest request;

    private final Gson gson = GsonFactory.getGson();

    public void put(String key, String str, long timeout) {
        if (eruptProp.isRedisSession()) {
            redisTemplate.opsForValue().set(key, str, timeout, TimeUnit.SECONDS);
        } else {
            request.getSession().setAttribute(key, str);
        }
    }

    public void putByLoginExpire(String key, String str) {
        if (eruptProp.isRedisSession()) {
            redisTemplate.opsForValue().set(key, str, eruptUpmsConfig.getExpireTimeByLogin(), TimeUnit.MINUTES);
        } else {
            request.getSession().setAttribute(key, str);
        }
    }

    public void remove(String key) {
        if (eruptProp.isRedisSession()) {
            redisTemplate.delete(key);
        } else {
            request.getSession().removeAttribute(key);
        }
    }

    public Object get(String key) {
        if (eruptProp.isRedisSession()) {
            return redisTemplate.opsForValue().get(key);
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
}
