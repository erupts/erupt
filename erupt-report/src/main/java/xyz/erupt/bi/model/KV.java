package xyz.erupt.bi.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author YuePeng
 * date 2023/12/7 23:35
 */
@Getter
@Setter
public class KV<K, V> {

    private K key;

    private V value;

    public KV(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public KV() {
    }
}
