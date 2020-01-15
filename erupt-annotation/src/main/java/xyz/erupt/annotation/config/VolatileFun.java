package xyz.erupt.annotation.config;

import com.google.gson.JsonElement;

public interface VolatileFun<T> {
    JsonElement exec(T t);
}
