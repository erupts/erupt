package xyz.erupt.core.session;

import java.util.concurrent.TimeUnit;

/**
 * Created by liyuepeng on 2019-08-13.
 */
public interface SessionService {

    void put(String key, Object obj, long timeout, TimeUnit unit);

    void remove(String key);

    Object get(String key);

    default void put(String key, Object obj, long timeout) {
        put(key, obj, timeout, TimeUnit.MINUTES);
    }
}
