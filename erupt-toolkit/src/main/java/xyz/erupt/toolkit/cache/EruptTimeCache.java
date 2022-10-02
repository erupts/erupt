package xyz.erupt.toolkit.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * LRU + TimeOut 缓存实现
 *
 * @author mxd
 */
public class EruptTimeCache<V> extends LinkedHashMap<String, EruptTimeCache.ExpireNode<V>> {

    private final int capacity;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public EruptTimeCache(int capacity) {
        super((int) Math.ceil(capacity / 0.75) + 1, 0.75f, true);
        // 容量
        this.capacity = capacity;
    }

    /**
     * @param ttl 单位：毫秒
     */
    public void put(String key, V value, long ttl) {
        long expireTime = ttl > 0 ? (System.currentTimeMillis() + ttl) : Long.MAX_VALUE;
        lock.writeLock().lock();
        try {
            // 封装成过期时间节点
            put(key, new ExpireNode<>(expireTime, value));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Object get(String key) {
        lock.readLock().lock();
        ExpireNode<V> expireNode;
        try {
            expireNode = super.get(key);
        } finally {
            lock.readLock().unlock();
        }
        if (expireNode == null) return null;
        if (expireNode.expire < System.currentTimeMillis()) {
            try {
                lock.writeLock().lock();
                super.remove(key);
            } finally {
                lock.writeLock().unlock();
            }
            return null;
        }
        return expireNode.value;
    }

    public void delete(String key) {
        try {
            lock.writeLock().lock();
            this.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, ExpireNode<V>> eldest) {
        if (this.size() > capacity) clean();
        return this.size() > this.capacity;
    }

    /**
     * 清理已过期的数据
     */
    private void clean() {
        try {
            lock.writeLock().lock();
            Iterator<Map.Entry<String, ExpireNode<V>>> iterator = super.entrySet().iterator();
            long now = System.currentTimeMillis();
            while (iterator.hasNext()) {
                Map.Entry<String, ExpireNode<V>> next = iterator.next();
                // 判断是否过期
                if (next.getValue().expire < now) iterator.remove();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * 过期时间节点
     */
    static class ExpireNode<V> {
        long expire;

        V value;

        ExpireNode(long expire, V value) {
            this.expire = expire;
            this.value = value;
        }
    }

}
