package xyz.erupt.core.cache;

import java.util.function.Function;

/**
 * @author YuePeng
 * date 2021/12/30 00:04
 */
public interface MetaCache<V> {

    V get(String key, Function<String, V> function);

}
