package xyz.erupt.core.proxy;

import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInvocation;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.core.proxy.erupt_field.EditProxy;
import xyz.erupt.core.proxy.erupt_field.ViewProxy;

/**
 * @author YuePeng
 * date 2022/2/5 14:19
 */
public class EruptFieldProxy extends AnnotationProxy<EruptField> {

    private final AnnotationProxy<Edit> editAnnotationProxy = new EditProxy();

    private final AnnotationProxy<View> viewAnnotationProxy = new ViewProxy();

    @Override
    @SneakyThrows
    protected Object invocation(MethodInvocation invocation) {
        if (Edit.class.getSimpleName().equals(invocation.getMethod().getReturnType().getSimpleName())) {
            return AnnotationProxyPool.getOrPut((Edit) this.invoke(invocation), annotation ->
                    editAnnotationProxy.newProxy(annotation, this, this.field));
        } else if ("views".equals(invocation.getMethod().getName())) {
            View[] views = (View[]) this.invoke(invocation);
            View[] proxyViews = new View[views.length];
            for (int i = 0; i < views.length; i++) {
                proxyViews[i] = AnnotationProxyPool.getOrPut(views[i], annotation ->
                        viewAnnotationProxy.newProxy(annotation, this, this.field));
            }
            return proxyViews;
        }
        return this.invoke(invocation);
    }

}
