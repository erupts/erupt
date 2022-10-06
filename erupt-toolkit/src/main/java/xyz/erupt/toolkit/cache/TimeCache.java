package xyz.erupt.toolkit.cache;

import xyz.erupt.core.toolkit.LRUCache;

/**
 * @author YuePeng
 * date 2022/10/3 00:04
 */
public class TimeCache<V> extends LRUCache<V> {

    private final long schedulePruneDelay;

    public TimeCache(int capacity, long schedulePruneDelay) {
        super(capacity);
        this.schedulePruneDelay = schedulePruneDelay;
    }

}
