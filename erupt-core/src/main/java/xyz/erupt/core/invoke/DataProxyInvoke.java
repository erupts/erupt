package xyz.erupt.core.invoke;

import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptModel;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author YuePeng
 * date 2021/3/16 13:37
 */
public class DataProxyInvoke {

    //@PreDataProxy的注解容器
    private static final Set<Class<? extends Annotation>> dataProxyAnnotationContainer = new HashSet<>();

    //注册支持@PreDataProxy注解的注解容器
    public static void registerAnnotationContainer(Class<? extends Annotation> annotationClass) {
        PreDataProxy preDataProxy = annotationClass.getAnnotation(PreDataProxy.class);
        if (preDataProxy == null) throw new RuntimeException("register error not found @PreDataProxy");
        dataProxyAnnotationContainer.add(annotationClass);
    }

    public static void invoke(EruptModel eruptModel, Consumer<DataProxy<Object>> consumer) {
        //注解中的 @PreDataProxy
        dataProxyAnnotationContainer.forEach(pc -> Optional.ofNullable(eruptModel.getClazz().getAnnotation(pc)).ifPresent(it -> {
            PreDataProxy preDataProxy = it.annotationType().getAnnotation(PreDataProxy.class);
            DataProxyContext.set(new DataProxyContext.Data(eruptModel, preDataProxy.params()));
            consumer.accept(getInstanceBean(preDataProxy.value()));
            DataProxyContext.remove();
        }));
        //父类及接口 @PreDataProxy
        ReflectUtil.findClassExtendStack(eruptModel.getClazz()).forEach(clazz -> DataProxyInvoke.actionInvokePreDataProxy(eruptModel, clazz, consumer));
        //本类及接口 @PreDataProxy
        DataProxyInvoke.actionInvokePreDataProxy(eruptModel, eruptModel.getClazz(), consumer);
        //@Erupt → DataProxy
        Stream.of(eruptModel.getErupt().dataProxy()).forEach(proxy -> {
            DataProxyContext.set(new DataProxyContext.Data(eruptModel, eruptModel.getErupt().dataProxyParams()));
            consumer.accept(getInstanceBean(proxy));
            DataProxyContext.remove();
        });
    }

    private static void actionInvokePreDataProxy(EruptModel eruptModel, Class<?> clazz, Consumer<DataProxy<Object>> consumer) {
        //接口
        Stream.of(clazz.getInterfaces()).forEach(it -> Optional.ofNullable(it.getAnnotation(PreDataProxy.class))
                .ifPresent(dataProxy -> {
                    DataProxyContext.set(new DataProxyContext.Data(eruptModel, dataProxy.params()));
                    consumer.accept(getInstanceBean(dataProxy.value()));
                    DataProxyContext.remove();
                }));
        //类
        Optional.ofNullable(clazz.getAnnotation(PreDataProxy.class))
                .ifPresent(dataProxy -> {
                    DataProxyContext.set(new DataProxyContext.Data(eruptModel, dataProxy.params()));
                    consumer.accept(getInstanceBean(dataProxy.value()));
                    DataProxyContext.remove();
                });
    }

    private static DataProxy<Object> getInstanceBean(Class<? extends DataProxy<?>> dataProxy) {
        return (DataProxy) EruptSpringUtil.getBean(dataProxy);
    }

}
