package xyz.erupt.auth.service;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import xyz.erupt.auth.config.EruptAuthConfig;
import xyz.erupt.core.config.EruptConfig;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

/**
 * @author liyuepeng
 * @date 2019-08-13.
 */
@Component
public class SessionService {

    @Autowired
    private EruptConfig eruptConfig;

    @Autowired
    private EruptAuthConfig eruptAuthConfig;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private Gson gson;

    public void put(String key, String str, long timeout) {
        if (eruptConfig.isRedisSession()) {
            redisTemplate.opsForValue().set(key, str, timeout, TimeUnit.SECONDS);
        } else {
            request.getSession().setAttribute(key, str);
        }
    }

    public void put(String key, String str) {
        if (eruptConfig.isRedisSession()) {
            redisTemplate.opsForValue().set(key, str, eruptAuthConfig.getExpireTimeByLogin(), TimeUnit.MINUTES);
        } else {
            request.getSession().setAttribute(key, str);
        }
    }

    public void remove(String key) {
        if (eruptConfig.isRedisSession()) {
            redisTemplate.delete(key);
        } else {
            request.getSession().removeAttribute(key);
        }
    }

    public Object get(String key) {
        if (eruptConfig.isRedisSession()) {
            return redisTemplate.opsForValue().get(key);
        } else {
            return request.getSession().getAttribute(key);
        }
    }

    public <T> T get(String key, Type type) {
        if (eruptConfig.isRedisSession()) {
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
