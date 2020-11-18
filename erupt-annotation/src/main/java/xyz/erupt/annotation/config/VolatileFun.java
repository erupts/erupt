package xyz.erupt.annotation.config;

import java.lang.annotation.Annotation;

/**
 * @param <A> statement annotation
 * @param <P> parent annotation
 */
public interface VolatileFun<A extends Annotation, P extends Annotation> {

    /**
     * @param a 当前注解
     * @param p 父级注解
     * @return json对象
     */
    Void exec(A a, P p);
}
