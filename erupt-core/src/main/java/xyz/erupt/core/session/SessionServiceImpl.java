package xyz.erupt.core.session;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import xyz.erupt.core.config.EruptConfig;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

/**
 * Created by liyuepeng on 2019-08-13.
 */
@Component
public class SessionServiceImpl implements SessionService {

    @Autowired
    private EruptConfig eruptConfig;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private Gson gson = new Gson();

    @Override
    public void put(String key, Object obj, long timeout, TimeUnit unit) {
        if (eruptConfig.isRedisSession()) {
            redisTemplate.opsForValue().set(key, gson.toJson(obj), timeout, unit);
        } else {
            request.getSession().setAttribute(key, obj);
        }
    }

    @Override
    public void remove(String key) {
        if (eruptConfig.isRedisSession()) {
            redisTemplate.delete(key);
        } else {
            request.getSession().removeAttribute(key);
        }
    }

    @Override
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
            return (T) request.getSession().getAttribute(key);
        }
    }

}
