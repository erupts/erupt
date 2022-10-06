package xyz.erupt.toolkit.cache.impl;

import xyz.erupt.core.toolkit.LRUCache;
import xyz.erupt.toolkit.cache.EruptCache;

/**
 * @author YuePeng
 * date 2022/1/26 22:41
 */
public class EruptStandaloneCache<V> extends EruptCache<V> {

    private final LRUCache<V> LRUCache = new LRUCache<>(2000);

    @Override
    protected V put(String key, long ttl, V v) {
        LRUCache.put(key, v, ttl);
        return v;
    }

    @Override
    protected V get(String key) {
        return LRUCache.get(key);
    }
}
