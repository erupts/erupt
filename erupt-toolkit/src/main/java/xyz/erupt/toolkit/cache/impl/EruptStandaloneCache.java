package xyz.erupt.toolkit.cache.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import xyz.erupt.toolkit.cache.EruptCache;

/**
 * @author YuePeng
 * date 2022/1/26 22:41
 */
public class EruptStandaloneCache<V> extends EruptCache<V> {

    private final TimedCache<String, V> cache = CacheUtil.newTimedCache(1000);

    public EruptStandaloneCache() {
        cache.schedulePrune(1000 * 60);
    }

    @Override
    protected V put(String key, long timeout, V v) {
        cache.put(key, v, timeout);
        return v;
    }

    @Override
    protected V get(String key) {
        return cache.get(key);
    }
}
