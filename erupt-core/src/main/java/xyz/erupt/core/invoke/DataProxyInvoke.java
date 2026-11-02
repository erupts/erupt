package xyz.erupt.core.invoke;

import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.interceptor.PostDataProxy;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptModel;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author YuePeng
 * date 2021/3/16 13:37
 */
public class DataProxyInvoke {

    // Annotation container for @PreDataProxy
    private static final Set<Class<? extends Annotation>> dataProxyAnnotationContainer = new HashSet<>();

    // Bumped on every container registration, so a chain resolved earlier knows it is stale
    private static volatile int containerVersion;

    // One resolved DataProxy of an erupt: the class to call and the params its annotation carries
    public record Entry(Class<? extends DataProxy<?>> proxy, String[] params) {
    }

    // The annotation-derived proxies of one erupt in call order, resolved once per model
    public record Chain(int version, List<Entry> entries) {
    }

    // Register an annotation container that supports the @PreDataProxy annotation
    public static void registerAnnotationContainer(Class<? extends Annotation> annotationClass) {
        PreDataProxy preDataProxy = annotationClass.getAnnotation(PreDataProxy.class);
        if (preDataProxy == null) throw new RuntimeException("register error not found @PreDataProxy");
        dataProxyAnnotationContainer.add(annotationClass);
        containerVersion++;
    }

    public static void invoke(EruptModel eruptModel, Consumer<DataProxy<Object>> consumer) {
        // a remote erupt's DataProxy runs on its node
        if (eruptModel.isRemote()) return;
        for (Entry entry : chainOf(eruptModel).entries()) {
            DataProxyContext.set(new DataProxyContext.Data(eruptModel, entry.params()));
            try {
                consumer.accept(getInstanceBean(entry.proxy()));
            } finally {
                DataProxyContext.remove();
            }
        }
        // POST DataProxy: read live, modules may still register them after the chain was built
        DataProxyContext.set(new DataProxyContext.Data(eruptModel, eruptModel.getErupt().dataProxyParams()));
        try {
            for (Class<? extends DataProxy<Object>> proxy : PostDataProxy.getDataProxies()) {
                consumer.accept(getInstanceBean(proxy));
            }
        } finally {
            DataProxyContext.remove();
        }
    }

    // The reflection over containers, class hierarchy and @Erupt happens once per model; the model
    // caches the result and a later container registration invalidates it through the version
    private static Chain chainOf(EruptModel eruptModel) {
        Chain chain = eruptModel.getDataProxyChain();
        if (null == chain || chain.version() != containerVersion) {
            chain = resolve(eruptModel);
            eruptModel.setDataProxyChain(chain);
        }
        return chain;
    }

    private static Chain resolve(EruptModel eruptModel) {
        int version = containerVersion;
        List<Entry> entries = new ArrayList<>();
        Class<?> clazz = eruptModel.getClazz();
        // The @PreDataProxy in the annotation
        dataProxyAnnotationContainer.forEach(pc -> Optional.ofNullable(clazz.getAnnotation(pc)).ifPresent(it -> {
            PreDataProxy preDataProxy = it.annotationType().getAnnotation(PreDataProxy.class);
            entries.add(new Entry(preDataProxy.value(), preDataProxy.params()));
        }));
        // Parent classes and interfaces @PreDataProxy
        ReflectUtil.findClassExtendStack(clazz).forEach(c -> collectPreDataProxy(c, entries));
        // This class and interface @PreDataProxy
        collectPreDataProxy(clazz, entries);
        // @Erupt → DataProxy
        Stream.of(eruptModel.getErupt().dataProxy()).forEach(proxy -> entries.add(new Entry(proxy, eruptModel.getErupt().dataProxyParams())));
        return new Chain(version, List.copyOf(entries));
    }

    private static void collectPreDataProxy(Class<?> clazz, List<Entry> entries) {
        // Interface
        Stream.of(clazz.getInterfaces()).forEach(it -> Optional.ofNullable(it.getAnnotation(PreDataProxy.class))
                .ifPresent(dataProxy -> entries.add(new Entry(dataProxy.value(), dataProxy.params()))));
        // Class
        Optional.ofNullable(clazz.getAnnotation(PreDataProxy.class))
                .ifPresent(dataProxy -> entries.add(new Entry(dataProxy.value(), dataProxy.params())));
    }

    @SuppressWarnings("unchecked")
    private static DataProxy<Object> getInstanceBean(Class<? extends DataProxy<?>> dataProxy) {
        return (DataProxy<Object>) EruptSpringUtil.getBean(dataProxy);
    }

}
