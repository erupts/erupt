package xyz.erupt.annotation.config;

import com.google.gson.JsonElement;

import java.lang.annotation.Annotation;

/**
 * @param <A> statement annotation
 * @param <P> parent annotation
 */
public interface VolatileFun<A, P extends Annotation> {

    /**
     * @param a 当前注解
     * @param p 父级注解
     * @return json对象
     */
    JsonElement exec(A a, P p);
}
