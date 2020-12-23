package xyz.erupt.annotation.config;

import java.lang.annotation.Annotation;

/**
 * @param <A> statement annotation
 * @param <P> parent annotation
 */
public interface VolatileFun<A extends Annotation, P extends Annotation> {

    @Comment("返回值：JSON对象")
    Void exec(@Comment("当前注解") A a, @Comment("父级注解") P p);

}
