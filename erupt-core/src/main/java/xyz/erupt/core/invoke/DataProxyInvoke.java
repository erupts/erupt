package xyz.erupt.core.invoke;

import xyz.erupt.annotation.PreDataProxy;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.util.EruptSpringUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptModel;

import java.util.function.Consumer;

/**
 * @author YuePeng
 * date 2021/3/16 13:37
 */
public class DataProxyInvoke {

    public static void invoke(EruptModel eruptModel, Consumer<DataProxy> consumer) {
        for (Class<?> clazz : ReflectUtil.findClassExtendStack(eruptModel.getClazz())) {
            DataProxyInvoke.invokeInterfaceDataProxy(clazz, consumer);
            PreDataProxy preDataProxy = clazz.getAnnotation(PreDataProxy.class);
            if (null != preDataProxy) {
                consumer.accept(EruptSpringUtil.getBean(preDataProxy.value()));
            }
        }
        DataProxyInvoke.invokeInterfaceDataProxy(eruptModel.getClazz(), consumer);

        PreDataProxy preDataProxy = eruptModel.getClazz().getAnnotation(PreDataProxy.class);
        if (null != preDataProxy) {
            consumer.accept(EruptSpringUtil.getBean(preDataProxy.value()));
        }

        for (Class<? extends DataProxy<?>> proxy : eruptModel.getErupt().dataProxy()) {
            consumer.accept(EruptSpringUtil.getBean(proxy));
        }
    }

    private static void invokeInterfaceDataProxy(Class<?> clazz, Consumer<DataProxy> consumer) {
        for (Class<?> ic : clazz.getInterfaces()) {
            PreDataProxy preDataProxy = ic.getAnnotation(PreDataProxy.class);
            if (null != preDataProxy) {
                consumer.accept(EruptSpringUtil.getBean(preDataProxy.value()));
            }
        }
    }

}
