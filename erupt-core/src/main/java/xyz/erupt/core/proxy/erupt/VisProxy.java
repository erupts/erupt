package xyz.erupt.core.proxy.erupt;

import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.Vis;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.core.proxy.AnnotationProxy;
import xyz.erupt.core.proxy.ProxyContext;

public class VisProxy extends AnnotationProxy<Vis, Erupt> {

    @Override
    protected Object invocation(MethodInvocation invocation) {
        if (super.matchMethod(invocation, Vis::code) && AnnotationConst.EMPTY_STR.equals(this.rawAnnotation.code())) {
            return Integer.toString(this.rawAnnotation.title().hashCode());
        } else if (super.matchMethod(invocation, Vis::title)) {
            return ProxyContext.translate(this.rawAnnotation.title());
        } else if (super.matchMethod(invocation, Vis::desc)) {
            return ProxyContext.translate(this.rawAnnotation.desc());
        } else if (super.matchMethod(invocation, Vis::filter)) {
            return FilterProxy.proxy(this.rawAnnotation.filter(), this);
        }
        return this.invoke(invocation);
    }

}
