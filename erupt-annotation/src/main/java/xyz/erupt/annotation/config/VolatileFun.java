package xyz.erupt.annotation.config;

import java.lang.annotation.Annotation;

/**
 * @param <A> statement annotation
 * @param <P> parent annotation
 */
public interface VolatileFun<A extends Annotation, P extends Annotation> {

    @Comment("Return value: JSON object")
    Void exec(@Comment("current annotation") A a, @Comment("parent annotation") P p);

}
