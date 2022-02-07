package xyz.erupt.core.proxy;

import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.sub_erupt.Filter;

/**
 * @author YuePeng
 * date 2022/2/5 14:19
 */
public class EruptProxy extends AnnotationProxy<Erupt> {

    private final AnnotationProxy<Filter> filterProxy = new FilterProxy();

    @Override
    @SneakyThrows
    protected Object invocation(MethodInvocation invocation) {
//        if ("filter".equals(invocation.getMethod().getName())) {
//            return AnnotationProxyPool.getOrPut((Filter) this.invoke(invocation), filter ->
//                    filterProxy.newProxy(filter, this, this.clazz));
//        }
        return this.invoke(invocation);
    }

}
