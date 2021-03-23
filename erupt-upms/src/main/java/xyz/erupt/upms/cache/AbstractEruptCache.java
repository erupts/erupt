package xyz.erupt.upms.cache;

import java.util.concurrent.TimeUnit;

/**
 * @author liyuepeng
 * @date 2021/3/23 13:25
 */
public abstract class AbstractEruptCache<V> implements IEruptCache<V> {

    protected long timeout;

    protected TimeUnit timeUnit;

    public AbstractEruptCache(long timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

}
