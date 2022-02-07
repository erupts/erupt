package xyz.erupt.core.proxy;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 代理池
 *
 * @author YuePeng
 * date 2022/2/7 10:27
 */
public class AnnotationProxyPool {

    /**
     * generic 1 raw annotation
     * generic 2 proxy annotation
     */
    private static final Map<Annotation, Annotation> annotationMap = new HashMap<>();

    public static <A extends Annotation> A getOrPut(A rawAnnotation, Function<A, A> function) {
        if (annotationMap.containsKey(rawAnnotation)) {
            return (A) annotationMap.get(rawAnnotation);
        }
        A proxyAnnotation = function.apply(rawAnnotation);
        annotationMap.put(rawAnnotation, proxyAnnotation);
        return proxyAnnotation;
    }

}
