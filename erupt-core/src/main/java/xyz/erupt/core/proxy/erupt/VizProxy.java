package xyz.erupt.core.proxy.erupt;

import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.Viz;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.core.proxy.AnnotationProxy;
import xyz.erupt.core.proxy.ProxyContext;

public class VizProxy extends AnnotationProxy<Viz, Erupt> {

    @Override
    protected Object invocation(MethodInvocation invocation) {
        if (super.matchMethod(invocation, Viz::code) && AnnotationConst.EMPTY_STR.equals(this.rawAnnotation.code())) {
            return Integer.toString(this.rawAnnotation.title().hashCode());
        } else if (super.matchMethod(invocation, Viz::title)) {
            return ProxyContext.translate(this.rawAnnotation.title());
        } else if (super.matchMethod(invocation, Viz::desc)) {
            return ProxyContext.translate(this.rawAnnotation.desc());
        } else if (super.matchMethod(invocation, Viz::filter)) {
            return FilterProxy.proxy(this.rawAnnotation.filter(), this);
        }
        return this.invoke(invocation);
    }

}
