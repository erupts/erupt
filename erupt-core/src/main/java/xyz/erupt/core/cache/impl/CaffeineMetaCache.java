package xyz.erupt.core.cache.impl;

import xyz.erupt.core.cache.MetaCache;

import java.util.function.Function;

/**
 * @author YuePeng
 * date 2021/12/30 00:06
 */
public class CaffeineMetaCache<T> implements MetaCache<T> {

    @Override
    public T get(String key, Function<String, T> function) {
        return null;
    }

}
