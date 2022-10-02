package xyz.erupt.toolkit.cache;

/**
 * @author YuePeng
 * date 2022/10/3 00:04
 */
public class EruptTimeCache<V> extends EruptLRUCache<V> {

    public EruptTimeCache(int capacity) {
        super(capacity);
    }

}
