package xyz.erupt.upms.cache;

import java.util.function.Function;

/**
 * @author YuePeng
 * date 2021/3/23 14:09
 */
public interface IEruptCache<V> {
    V get(String key, Function<String, V> function);
}
