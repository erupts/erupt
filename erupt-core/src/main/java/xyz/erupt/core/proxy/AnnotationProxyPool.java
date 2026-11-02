package xyz.erupt.core.proxy;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Proxy Cache Pool
 *
 * @author YuePeng
 * date 2022/2/7 10:27
 */
public class AnnotationProxyPool {

    /**
     * generic key raw annotation
     * generic value proxy annotation
     */
    private static final Map<Annotation, Annotation> annotationPool = new HashMap<>();

    // The same raw annotation instance comes back on every call, so after the first value-equal lookup
    // (which hashes every member, nested annotations included) it is found again by identity
    private static final Map<Identity, Annotation> identityPool = new ConcurrentHashMap<>();

    public static <A extends Annotation> A getOrPut(A rawAnnotation, Function<A, A> function) {
        Annotation hit = identityPool.get(new Identity(rawAnnotation));
        if (null != hit) return (A) hit;
        synchronized (annotationPool) {
            A proxyAnnotation = (A) annotationPool.get(rawAnnotation);
            if (null == proxyAnnotation) {
                proxyAnnotation = function.apply(rawAnnotation);
                annotationPool.put(rawAnnotation, proxyAnnotation);
            }
            identityPool.put(new Identity(rawAnnotation), proxyAnnotation);
            return proxyAnnotation;
        }
    }

    private record Identity(Annotation annotation) {
        @Override
        public boolean equals(Object o) {
            return o instanceof Identity other && other.annotation == annotation;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(annotation);
        }
    }

}
