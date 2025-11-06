package xyz.erupt.core.proxy.erupt;

import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.annotation.fun.FilterHandler;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.core.proxy.AnnotationProxy;
import xyz.erupt.core.proxy.AnnotationProxyPool;
import xyz.erupt.core.util.EruptSpringUtil;

/**
 * @author YuePeng
 * date 2022/2/6 19:38
 */
public class FilterProxy<P> extends AnnotationProxy<Filter, P> {

    @Override
    protected Object invocation(MethodInvocation invocation) {
        if (super.matchMethod(invocation, Filter::value)) {
            String condition = this.rawAnnotation.value();
            if (!this.rawAnnotation.conditionHandler().isInterface()) {
                FilterHandler ch = EruptSpringUtil.getBean(this.rawAnnotation.conditionHandler());
                condition = ch.filter(condition, this.rawAnnotation.params());
            }
            return condition;
        }
        return this.invoke(invocation);
    }

    public static <A, PA> Filter[] proxy(Filter[] filters, AnnotationProxy<A, PA> parent) {
        Filter[] proxyFilters = new Filter[filters.length];
        for (int i = 0; i < filters.length; i++) {
            proxyFilters[i] = AnnotationProxyPool.getOrPut(filters[i], filter -> new FilterProxy<A>().newProxy(filter, parent));
        }
        return proxyFilters;
    }

}
